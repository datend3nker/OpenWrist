package io.github.datend3nker.openwrist.presentation

import androidx.lifecycle.ViewModel
import io.github.datend3nker.openwrist.repo.WatchApps
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.fdroid.database.AppListItem

class SearchViewModel(
    private val watchApps: WatchApps,
) : ViewModel() {
    private val mutableQuery = MutableStateFlow("")

    /**
     * Watch apps matching the typed text, or null while it is empty. The
     * search waits until typing pauses, so not every key press runs a query.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val results: Flow<List<AppListItem>?> =
        mutableQuery
            .map { it.trim() }
            .distinctUntilChanged()
            .debounce { if (it.isEmpty()) 0L else TYPING_PAUSE_MS }
            .flatMapLatest { query ->
                if (query.isEmpty()) flowOf(null) else watchApps.search(query)
            }

    /** Call with the text of the search field whenever it changes. */
    fun search(text: String) {
        mutableQuery.value = text
    }

    private companion object {
        const val TYPING_PAUSE_MS = 300L
    }
}
