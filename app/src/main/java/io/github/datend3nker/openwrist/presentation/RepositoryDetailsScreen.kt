package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.datend3nker.openwrist.R

/**
 * Name, address and key fingerprint of a repository. Repositories the user
 * added can be removed here, after confirming in a dialog. Goes back with
 * [onRemoved] once the repository is gone.
 */
@Composable
fun RepositoryDetailsScreen(
    repoId: Long,
    onRemoved: () -> Unit,
) {
    val viewModel =
        containerViewModel(key = repoId.toString()) { RepositoryDetailsViewModel(it.repoManager, it.appScope, repoId) }
    val repository by viewModel.repository.collectAsStateWithLifecycle()
    var askRemove by rememberSaveable { mutableStateOf(false) }
    val listState = rememberTransformingLazyColumnState()

    val current = repository
    LaunchedEffect(current == null) {
        if (current == null) onRemoved()
    }
    if (current == null) return
    val name = current.getName(LocaleListCompat.getDefault()) ?: current.address

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(name, textAlign = TextAlign.Center) } }
            item { CenteredText(current.address, small = true) }
            item {
                val fingerprint = formatFingerprint(current.fingerprint)
                CenteredText(stringResource(R.string.add_repository_fingerprint, fingerprint), small = true)
            }
            if (viewModel.canRemove(current)) {
                item {
                    OutlinedButton(
                        onClick = { askRemove = true },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.repository_remove)) },
                    )
                }
            } else {
                item { CenteredText(stringResource(R.string.repository_cannot_remove), small = true) }
            }
        }
    }
    AlertDialog(
        visible = askRemove,
        onDismissRequest = { askRemove = false },
        confirmButton = {
            AlertDialogDefaults.ConfirmButton(
                onClick = {
                    askRemove = false
                    viewModel.remove()
                },
            )
        },
        title = { Text(stringResource(R.string.repository_remove_title, name)) },
        text = { Text(stringResource(R.string.repository_remove_text)) },
    )
}
