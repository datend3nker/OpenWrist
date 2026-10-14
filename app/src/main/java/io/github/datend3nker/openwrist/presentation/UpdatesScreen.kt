package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.datend3nker.openwrist.R

/** Installed apps with a newer watch build. Tapping one opens its details, where it is updated. */
@Composable
fun UpdatesScreen(onAppClick: (packageName: String) -> Unit) {
    val viewModel = containerViewModel { UpdatesViewModel(it.watchApps) }
    val updates by viewModel.updates.collectAsStateWithLifecycle()
    val listState = rememberTransformingLazyColumnState()
    LifecycleResumeEffect(Unit) {
        viewModel.load()
        onPauseOrDispose {}
    }

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.updates)) } }
            val list = updates
            when {
                list == null -> item { CircularProgressIndicator() }
                list.isEmpty() ->
                    item {
                        Text(
                            stringResource(R.string.updates_none),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                else ->
                    items(list, key = { it.packageName }) { app ->
                        Button(
                            onClick = { onAppClick(app.packageName) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(),
                            icon = {
                                AppIcon(
                                    app.repoId,
                                    app.getIcon(LocaleListCompat.getDefault()),
                                    ButtonDefaults.LargeIconSize,
                                )
                            },
                            label = {
                                Text(app.name ?: app.packageName, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            },
                            secondaryLabel = {
                                Text(
                                    stringResource(
                                        R.string.details_update_available,
                                        app.installedVersionName,
                                        app.update.versionName,
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
            }
        }
    }
}
