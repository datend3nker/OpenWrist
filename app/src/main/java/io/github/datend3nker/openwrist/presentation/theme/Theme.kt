package io.github.datend3nker.openwrist.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

/** OpenWrist's theme. Uses the Wear Material 3 defaults until there is a design. */
@Composable
fun OpenWristTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
