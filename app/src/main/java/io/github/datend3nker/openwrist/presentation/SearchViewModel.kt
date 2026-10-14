package io.github.datend3nker.openwrist.presentation

import androidx.lifecycle.ViewModel
import io.github.datend3nker.openwrist.repo.WatchApps
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.fdroid.database.AppListItem

class SearchViewModel(
    private val watchApps: WatchApps,
) : ViewModel() {
    private val mutableQuery = MutableStateFlow("")

    /** The last searched text, empty before the first search. */
    val query: StateFlow<String> = mutableQuery.asStateFlow()

    /** Watch apps matching the last search, or null before the first search. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val results: Flow<List<AppListItem>?> =
        mutableQuery.flatMapLatest { query ->
            if (query.isBlank()) flowOf(null) else watchApps.search(query)
        }

    fun search(text: String) {
        mutableQuery.value = text.trim()
    }
}
