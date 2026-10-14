package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import io.github.datend3nker.openwrist.repo.RefreshState

@Composable
fun AppListScreen(
    onAppClick: (packageName: String) -> Unit,
    onSearchClick: () -> Unit,
) {
    val viewModel =
        containerViewModel { AppListViewModel(it.watchApps, it.repoRefresher, it.appScope) }
    val apps by viewModel.apps.collectAsStateWithLifecycle(initialValue = null)
    val refreshState by viewModel.refreshState.collectAsStateWithLifecycle()
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.app_name)) } }
            item {
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.search)) },
                )
            }
            item { RefreshButton(refreshState, onRefresh = viewModel::refresh) }
            val list = apps
            if (list != null && list.isEmpty() && refreshState !is RefreshState.Running) {
                item {
                    Text(
                        stringResource(R.string.app_list_empty),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(list.orEmpty(), key = { it.packageName }) { app ->
                AppItem(app, onClick = { onAppClick(app.packageName) })
            }
            item {
                Text(
                    stringResource(R.string.unofficial_client),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun RefreshButton(
    state: RefreshState,
    onRefresh: () -> Unit,
) {
    when (state) {
        is RefreshState.Running ->
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    // Icon size, so the label next to it stays readable.
                    val size = Modifier.size(ButtonDefaults.IconSize)
                    val progress = state.progress
                    if (progress == null) {
                        CircularProgressIndicator(modifier = size)
                    } else {
                        CircularProgressIndicator(progress = { progress }, modifier = size)
                    }
                },
                label = { Text(stringResource(R.string.refreshing)) },
            )
        is RefreshState.Failed ->
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text(stringResource(R.string.refresh_failed)) },
                secondaryLabel = {
                    Text(state.repoAddresses.joinToString(), maxLines = 2, overflow = TextOverflow.Ellipsis)
                },
            )
        RefreshState.Idle ->
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text(stringResource(R.string.refresh)) },
            )
    }
}
