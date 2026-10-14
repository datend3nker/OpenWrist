package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

/**
 * A one-line text field for the round screen; Wear Material 3 has none.
 * Tapping it opens the keyboard, and the keyboard's action key closes it.
 *
 * The field owns its text in [state]. Callers read it from there instead of
 * passing it through a view model's flow and back, which would let the field
 * fall behind the keyboard while typing, so the keyboard inserts text twice.
 */
@Composable
fun TextInput(
    state: TextFieldState,
    hint: String,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    BasicTextField(
        state = state,
        modifier = modifier.fillMaxWidth(),
        lineLimits = TextFieldLineLimits.SingleLine,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = keyboardOptions,
        onKeyboardAction = { focusManager.clearFocus() },
        decorator = { innerTextField ->
            Box(
                Modifier
                    .background(colors.surfaceContainer, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                if (state.text.isEmpty()) Text(hint, color = colors.onSurfaceVariant)
                innerTextField()
            }
        },
    )
}
