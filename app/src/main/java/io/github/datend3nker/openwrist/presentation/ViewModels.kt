package io.github.datend3nker.openwrist.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.datend3nker.openwrist.AppContainer
import io.github.datend3nker.openwrist.OpenWristApplication

/**
 * Returns the [ViewModel] of the current screen, created by [create] with
 * the app's [AppContainer] the first time.
 */
@Composable
inline fun <reified VM : ViewModel> containerViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM,
): VM {
    val container = (LocalContext.current.applicationContext as OpenWristApplication).container
    return viewModel(key = key, factory = viewModelFactory { initializer { create(container) } })
}
