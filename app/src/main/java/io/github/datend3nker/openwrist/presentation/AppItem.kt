package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import org.fdroid.database.AppListItem

/** One app in a list: name and summary. Used by the app list and the search. */
@Composable
fun AppItem(
    app: AppListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.filledTonalButtonColors(),
        label = { Text(app.name ?: app.packageName, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        secondaryLabel = {
            val summary = app.summary
            if (summary != null) Text(summary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
    )
}
