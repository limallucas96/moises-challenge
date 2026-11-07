package com.lucaslima.musicsearch.core.playback

import android.app.PendingIntent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * Dono do ExoPlayer e da MediaLibrarySession. Roda em primeiro plano enquanto toca, o que dá a
 * notificação, os controles da tela de bloqueio e o áudio com o app fechado.
 *
 * Quem se conecta: o próprio app (pelo [Media3AudioPlayer]), o sistema e o Android Auto, que
 * navega pela [MediaTree] e pede músicas por voz.
 */
class PlaybackService :
    MediaLibraryService(),
    KoinComponent {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var session: MediaLibrarySession? = null

    /** Opcional: um app sem catálogo ainda toca, só não tem o que mostrar no carro. */
    private val tree by lazy { MediaTree(getKoin().getOrNull<MediaCatalog>()) }

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                // Pede o foco de áudio: pausa numa ligação e abaixa o volume numa notificação.
                true,
            )
            // Fone desconectado: pausa em vez de tocar no alto-falante.
            .setHandleAudioBecomingNoisy(true)
            .build()
        session = MediaLibrarySession.Builder(this, player, LibraryCallback())
            .apply { launchAppIntent()?.let(::setSessionActivity) }
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? = session

    override fun onDestroy() {
        scope.cancel()
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    /** Tocar na notificação abre o app. */
    private fun launchAppIntent(): PendingIntent? {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    /** Roda [block] no [scope] e entrega o resultado como o future que o Media3 espera. */
    private fun <T> future(
        fallback: T,
        block: suspend () -> T,
    ): ListenableFuture<T> {
        val future = SettableFuture.create<T>()
        scope.launch {
            try {
                future.set(block())
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                future.set(fallback)
            }
        }
        return future
    }

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(LibraryResult.ofItem(tree.root, params))

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
            future(LibraryResult.ofItemList(ImmutableList.of(), params)) {
                LibraryResult.ofItemList(tree.children(parentId).page(page, pageSize), params)
            }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String,
        ): ListenableFuture<LibraryResult<MediaItem>> =
            future(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)) {
                tree.item(mediaId)?.let { LibraryResult.ofItem(it, null) }
                    ?: LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
            }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<Void>> = future(LibraryResult.ofVoid()) {
            val results = tree.search(query)
            session.notifySearchResultChanged(browser, query, results.size, params)
            LibraryResult.ofVoid()
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?,
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
            future(LibraryResult.ofItemList(ImmutableList.of(), params)) {
                LibraryResult.ofItemList(tree.search(query).page(page, pageSize), params)
            }

        /**
         * Três origens: o app (itens já com endereço), um nó da árvore tocado no Auto (vira a
         * seção inteira a partir dele) e um pedido de voz (vira a busca).
         */
        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val single = mediaItems.singleOrNull()
            val voiceQuery = single?.requestMetadata?.searchQuery
            val fromTree = single?.mediaId?.let(MediaTree::parseItemId) != null
            if (voiceQuery == null && !fromTree) {
                return Futures.immediateFuture(
                    MediaSession.MediaItemsWithStartPosition(mediaItems.withAudioUri(), startIndex, startPositionMs),
                )
            }
            val empty = MediaSession.MediaItemsWithStartPosition(emptyList(), 0, C.TIME_UNSET)
            return future(empty) {
                val queue = if (voiceQuery != null) tree.queueForVoice(voiceQuery) else tree.queueFor(single.mediaId)
                queue?.let { mediaSession.player.playlistMetadata = MediaMetadata.Builder().setTitle(it.title).build() }
                queue?.toStartPosition() ?: empty
            }
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> = Futures.immediateFuture(mediaItems.withAudioUri())

        /** "Continuar tocando" pelo carro ou pelo fone, sem o app aberto. */
        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            isForPlayback: Boolean,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val empty = MediaSession.MediaItemsWithStartPosition(emptyList(), 0, C.TIME_UNSET)
            return future(empty) { tree.resumptionQueue()?.toStartPosition() ?: empty }
        }
    }
}

private fun MediaTree.Queue.toStartPosition() =
    MediaSession.MediaItemsWithStartPosition(items.map { it.toMediaItem() }, startIndex, C.TIME_UNSET)

/** O endereço do áudio volta de `requestMetadata.mediaUri` (ver [toMediaItem]). */
private fun List<MediaItem>.withAudioUri(): MutableList<MediaItem> = mapTo(mutableListOf()) { item ->
    val uri = item.requestMetadata.mediaUri
    if (item.localConfiguration == null && uri != null) item.buildUpon().setUri(uri).build() else item
}

private fun List<MediaItem>.page(
    page: Int,
    pageSize: Int,
): ImmutableList<MediaItem> {
    if (pageSize <= 0 || pageSize == Int.MAX_VALUE) return ImmutableList.copyOf(this)
    return ImmutableList.copyOf(drop(page * pageSize).take(pageSize))
}
