package com.lucaslima.musicsearch.feature.library.presentation.fakes

import androidx.paging.PagingData
import com.lucaslima.musicsearch.feature.library.domain.Song
import com.lucaslima.musicsearch.feature.library.domain.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf

/** Guarda os termos buscados; os recentes só chegam quando o teste emite em [recents]. */
class FakeSongRepository : SongRepository {
    val searchedTerms = mutableListOf<String>()
    val recents = MutableSharedFlow<List<Song>>(replay = 1)
    var resultsFor: (String) -> List<Song> = { emptyList() }

    override fun searchSongs(query: String): Flow<PagingData<Song>> {
        searchedTerms += query
        return flowOf(PagingData.from(resultsFor(query)))
    }

    override fun recentlyPlayed(limit: Int): Flow<List<Song>> = recents
}
