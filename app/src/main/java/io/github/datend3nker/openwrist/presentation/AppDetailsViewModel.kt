package io.github.datend3nker.openwrist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.datend3nker.openwrist.repo.AppDetails
import io.github.datend3nker.openwrist.repo.WatchApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** What the details screen shows. */
sealed interface DetailsState {
    data object Loading : DetailsState

    /** No enabled repository has the app (any more). */
    data object NotFound : DetailsState

    data class Loaded(
        val details: AppDetails,
    ) : DetailsState
}

class AppDetailsViewModel(
    private val watchApps: WatchApps,
    private val packageName: String,
) : ViewModel() {
    private val mutableState = MutableStateFlow<DetailsState>(DetailsState.Loading)
    val state: StateFlow<DetailsState> = mutableState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val details = watchApps.details(packageName)
            mutableState.value = if (details == null) DetailsState.NotFound else DetailsState.Loaded(details)
        }
    }
}
