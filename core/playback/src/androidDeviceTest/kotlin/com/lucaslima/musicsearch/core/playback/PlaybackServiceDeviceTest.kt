package com.lucaslima.musicsearch.core.playback

import android.content.ComponentName
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionToken
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * O [PlaybackService] visto de fora, como o Android Auto vê: um MediaBrowser de verdade navega
 * pela árvore, busca e pede música por voz. O catálogo é falso; o player não chega a tocar.
 */
class PlaybackServiceDeviceTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private lateinit var browser: MediaBrowser
    private val catalog = FakeMediaCatalog()
    private val changedParents = mutableListOf<String>()

    /** O Media3 já avisa uma vez ao assinar; a segunda é a que o catálogo disparou. */
    private val subscribed = CountDownLatch(1)
    private val childrenChanged = CountDownLatch(2)

    @BeforeTest
    fun setUp() {
        startKoin { modules(module { single<MediaCatalog> { catalog } }) }
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val listener = object : MediaBrowser.Listener {
            override fun onChildrenChanged(
                browser: MediaBrowser,
                parentId: String,
                itemCount: Int,
                params: MediaLibraryService.LibraryParams?,
            ) {
                changedParents += parentId
                subscribed.countDown()
                childrenChanged.countDown()
            }
        }
        browser = onMain { MediaBrowser.Builder(context, token).setListener(listener).buildAsync() }.await().await()
    }

    @AfterTest
    fun tearDown() {
        onMain { browser.release() }
        stopKoin()
    }

    @Test
    fun raiz_lista_uma_pasta_por_secao_do_catalogo() {
        val root = onMain { browser.getLibraryRoot(null) }.await().await().value!!
        val sections = onMain { browser.getChildren(root.mediaId, 0, 50, null) }.await().await().value!!

        assertThat(sections.map { it.mediaMetadata.title.toString() }).containsExactly("Recently played", "Last search")
        assertThat(sections.all { it.mediaMetadata.isBrowsable == true }).isTrue()
    }

    @Test
    fun secao_lista_as_musicas_tocaveis() {
        val songs = children(sectionId = "recent")

        assertThat(songs.map { it.mediaMetadata.title.toString() }).containsExactly("One More Time", "Aerodynamic")
        assertThat(songs.all { it.mediaMetadata.isPlayable == true }).isTrue()
    }

    @Test
    fun paginas_dividem_a_secao() {
        val first = onMain { browser.getChildren("section|recent", 0, 1, null) }.await().await().value!!
        val second = onMain { browser.getChildren("section|recent", 1, 1, null) }.await().await().value!!

        assertThat(first.map { it.mediaMetadata.title.toString() }).containsExactly("One More Time")
        assertThat(second.map { it.mediaMetadata.title.toString() }).containsExactly("Aerodynamic")
    }

    @Test
    fun busca_devolve_os_resultados_do_catalogo() {
        onMain { browser.search("daft", null) }.await().await()
        val results = onMain { browser.getSearchResult("daft", 0, 50, null) }.await().await().value!!

        assertThat(results.map { it.mediaMetadata.title.toString() }).containsExactly("Around the World")
    }

    @Test
    fun tocar_uma_musica_da_arvore_toca_a_secao_a_partir_dela() {
        val second = children(sectionId = "recent")[1]

        onMain { browser.setMediaItem(second) }
        awaitQueueSize(2)

        assertThat(onMain { browser.currentMediaItemIndex }.await()).isEqualTo(1)
        assertThat(onMain { browser.currentMediaItem?.mediaId }.await()).isEqualTo("2")
        assertThat(onMain { browser.playlistMetadata.title.toString() }.await()).isEqualTo("Recently played")
    }

    @Test
    fun pedido_de_voz_vira_a_fila_da_busca() {
        val request = MediaItem.Builder()
            .setRequestMetadata(MediaItem.RequestMetadata.Builder().setSearchQuery("daft").build())
            .build()

        onMain { browser.setMediaItem(request) }
        awaitQueueSize(1)

        assertThat(onMain { browser.currentMediaItem?.mediaId }.await()).isEqualTo("3")
        assertThat(onMain { browser.playlistMetadata.title.toString() }.await()).isEqualTo("daft")
    }

    @Test
    fun secao_que_muda_avisa_quem_assinou() {
        onMain { browser.subscribe("section|recent", null) }.await().await()
        assertThat(subscribed.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()

        catalog.changes.tryEmit("recent")

        assertThat(childrenChanged.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue()
        assertThat(onMain { changedParents.toList() }.await()).containsExactly("section|recent", "section|recent")
    }

    private fun children(sectionId: String): List<MediaItem> =
        onMain { browser.getChildren("section|$sectionId", 0, 50, null) }.await().await().value!!

    /** O MediaBrowser só aceita chamadas na thread em que nasceu: a principal. */
    private fun <T> onMain(block: () -> T): Future<T> {
        val task = FutureTask(Callable(block))
        instrumentation.runOnMainSync(task)
        return task
    }

    private fun <T> Future<T>.await(): T = get(TIMEOUT_SECONDS, TimeUnit.SECONDS)

    /** Espera o serviço montar a fila: o player avisa o browser por evento. */
    private fun awaitQueueSize(size: Int) {
        val reached = CountDownLatch(1)
        val listener = object : Player.Listener {
            override fun onEvents(
                player: Player,
                events: Player.Events,
            ) {
                if (player.mediaItemCount == size) reached.countDown()
            }
        }
        onMain {
            browser.addListener(listener)
            if (browser.mediaItemCount == size) reached.countDown()
        }.await()
        val arrived = reached.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        onMain { browser.removeListener(listener) }.await()
        check(arrived) { "a fila não chegou a $size itens" }
    }

    private companion object {
        const val TIMEOUT_SECONDS = 5L
    }
}

private class FakeMediaCatalog : MediaCatalog {
    /** O teste emite aqui para simular uma seção que mudou. */
    val changes = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun sectionChanges(): Flow<String> = changes

    private val recent = listOf(item(1, "One More Time"), item(2, "Aerodynamic"))

    override fun sections() = listOf(
        CatalogSection(id = "recent", title = "Recently played"),
        CatalogSection(id = "last-search", title = "Last search"),
    )

    override suspend fun items(sectionId: String) = if (sectionId == "recent") recent else emptyList()

    override suspend fun search(query: String) = listOf(item(3, "Around the World"))

    private fun item(
        id: Int,
        title: String,
    ) = PlayableItem(
        id = id.toString(),
        title = title,
        artist = "Daft Punk",
        albumTitle = "Discovery",
        artworkUrl = null,
        audioUrl = "https://audio.example/$id.m4a",
    )
}
