package com.lucaslima.musicsearch.core.playback

import android.app.PendingIntent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Dono do ExoPlayer e da MediaSession. Roda em primeiro plano enquanto toca, o que dá a
 * notificação, os controles da tela de bloqueio e o áudio com o app fechado. O app fala com ele
 * por um MediaController ([Media3AudioPlayer]), como qualquer outro cliente.
 */
class PlaybackService : MediaSessionService() {

    private var session: MediaSession? = null

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
        session = MediaSession.Builder(this, player)
            .setCallback(SessionCallback)
            .apply { launchAppIntent()?.let(::setSessionActivity) }
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
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

    private object SessionCallback : MediaSession.Callback {
        /**
         * O MediaItem chega do controller sem o endereço do áudio (o Media3 não repassa
         * `localConfiguration`). O endereço vai em `requestMetadata.mediaUri` e volta aqui.
         */
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> = Futures.immediateFuture(
            mediaItems.mapTo(mutableListOf()) { item ->
                val uri = item.requestMetadata.mediaUri
                if (item.localConfiguration == null && uri != null) item.buildUpon().setUri(uri).build() else item
            },
        )
    }
}
