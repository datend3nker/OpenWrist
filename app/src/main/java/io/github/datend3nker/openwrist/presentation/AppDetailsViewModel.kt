package io.github.datend3nker.openwrist.presentation

import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.datend3nker.openwrist.install.ApkInstaller
import io.github.datend3nker.openwrist.install.InstallManager
import io.github.datend3nker.openwrist.install.InstallState
import io.github.datend3nker.openwrist.repo.AppDetails
import io.github.datend3nker.openwrist.repo.WatchApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    private val installManager: InstallManager,
    private val apkInstaller: ApkInstaller,
    private val packageManager: PackageManager,
    private val packageName: String,
) : ViewModel() {
    private val mutableState = MutableStateFlow<DetailsState>(DetailsState.Loading)
    val state: StateFlow<DetailsState> = mutableState.asStateFlow()

    /** The running or last installation of this app, null if there is none. */
    val installState: StateFlow<InstallState?> =
        installManager.states
            .map { it[packageName] }
            .stateIn(viewModelScope, SharingStarted.Eagerly, installManager.states.value[packageName])

    private val mutableCanInstall = MutableStateFlow(apkInstaller.canInstall())

    /** False while the user has not allowed OpenWrist to install apps. */
    val canInstall: StateFlow<Boolean> = mutableCanInstall.asStateFlow()

    init {
        load()
        // After an installation the installed version changed.
        viewModelScope.launch {
            installState.filter { it == InstallState.Installed }.collect { load() }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val details = watchApps.details(packageName)
            mutableState.value = if (details == null) DetailsState.NotFound else DetailsState.Loaded(details)
        }
    }

    /** Call when the screen is shown again: the user may have changed the permission. */
    fun checkCanInstall() {
        mutableCanInstall.value = apkInstaller.canInstall()
    }

    fun install() {
        val details = (mutableState.value as? DetailsState.Loaded)?.details
        val version = details?.suggestedVersion
        val repository = details?.repository
        if (version != null && repository != null) installManager.install(repository, version)
    }

    fun clearInstallState() = installManager.clearState(packageName)

    /** The intent that starts the installed app, null if it is not installed or has no launcher entry. */
    fun launchIntent(): Intent? = packageManager.getLaunchIntentForPackage(packageName)
}
