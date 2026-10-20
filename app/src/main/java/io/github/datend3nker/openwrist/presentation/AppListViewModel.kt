package io.github.datend3nker.openwrist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.datend3nker.openwrist.repo.RefreshState
import io.github.datend3nker.openwrist.repo.RepoRefresher
import io.github.datend3nker.openwrist.repo.WatchApps
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.fdroid.database.AppListItem

/** The start screen: all watch apps, a search button and the refresh state. */
class AppListViewModel(
    watchApps: WatchApps,
    private val refresher: RepoRefresher,
    private val appScope: CoroutineScope,
) : ViewModel() {
    val apps: Flow<List<AppListItem>> = watchApps.appList()
    val refreshState: StateFlow<RefreshState> = refresher.state

    init {
        viewModelScope.launch {
            if (refresher.isRefreshDue()) refresh()
        }
    }

    /** Runs in [appScope], so it goes on when the user leaves the screen. */
    fun refresh() {
        appScope.launch { refresher.refresh() }
    }
}
