package io.github.datend3nker.openwrist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.datend3nker.openwrist.repo.WatchApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.fdroid.database.UpdatableApp

class UpdatesViewModel(
    private val watchApps: WatchApps,
) : ViewModel() {
    private val mutableUpdates = MutableStateFlow<List<UpdatableApp>?>(null)

    /** Installed apps with a newer watch build, null while loading. */
    val updates: StateFlow<List<UpdatableApp>?> = mutableUpdates.asStateFlow()

    /** Call when the screen is shown, apps may have been updated meanwhile. */
    fun load() {
        viewModelScope.launch { mutableUpdates.value = watchApps.updates() }
    }
}
