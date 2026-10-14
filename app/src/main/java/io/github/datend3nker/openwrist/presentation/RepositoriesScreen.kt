package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.Text
import io.github.datend3nker.openwrist.R

/** The repositories OpenWrist knows, each with a switch to enable or disable it. */
@Composable
fun RepositoriesScreen() {
    val viewModel = containerViewModel { RepositoriesViewModel(it.repoManager, it.repoRefresher, it.appScope) }
    val repositories by viewModel.repositories.collectAsStateWithLifecycle()
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.repositories)) } }
            items(repositories, key = { it.repoId }) { repository ->
                SwitchButton(
                    checked = repository.enabled,
                    onCheckedChange = { viewModel.setEnabled(repository, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            repository.getName(LocaleListCompat.getDefault()) ?: repository.address,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    secondaryLabel = {
                        Text(
                            repository.address
                                .toUri()
                                .host
                                .orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }
    }
}
