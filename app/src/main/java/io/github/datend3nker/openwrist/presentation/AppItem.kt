package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.os.LocaleListCompat
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import org.fdroid.database.AppListItem

/** One app in a list: icon, name and summary. Used by the app list and the search. */
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
        icon = { AppIcon(app.repoId, app.getIcon(LocaleListCompat.getDefault()), ButtonDefaults.LargeIconSize) },
        label = { Text(app.name ?: app.packageName, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        secondaryLabel = {
            val summary = app.summary
            if (summary != null) Text(summary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
    )
}
