package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator

/**
 * A progress indicator in icon size, for the icon slot of a button.
 * [progress] is between 0 and 1, or null if it is not known.
 *
 * While the screen is not in front, the indicator for unknown progress
 * stands still. Android's install and permission dialogs are translucent,
 * so the screen behind them stays started and would otherwise draw the
 * spinning animation at full frame rate, which costs a lot of battery on a
 * watch. Measured on a Galaxy Watch 7: about 80% of a CPU core.
 */
@Composable
fun ProgressIcon(progress: Float?) {
    val size = Modifier.size(ButtonDefaults.IconSize)
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    when {
        progress != null -> CircularProgressIndicator(progress = { progress }, modifier = size)
        lifecycleState.isAtLeast(Lifecycle.State.RESUMED) -> CircularProgressIndicator(modifier = size)
        else -> CircularProgressIndicator(progress = { PAUSED_PROGRESS }, modifier = size)
    }
}

/** How much of the ring a paused indicator shows, so it still looks like a spinner. */
private const val PAUSED_PROGRESS = 0.25f
