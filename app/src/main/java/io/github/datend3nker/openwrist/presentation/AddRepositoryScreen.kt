package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.datend3nker.openwrist.R
import org.fdroid.repo.AddRepoError
import org.fdroid.repo.AddRepoError.ErrorType
import org.fdroid.repo.Added
import org.fdroid.repo.Adding
import org.fdroid.repo.FetchResult
import org.fdroid.repo.Fetching
import org.fdroid.repo.None

/**
 * Adds a repository by its address: enter it, check the preview with the
 * repository's name and key fingerprint, then add it. Goes back with
 * [onAdded] once the repository is stored.
 */
@Composable
fun AddRepositoryScreen(onAdded: () -> Unit) {
    val viewModel = containerViewModel { AddRepositoryViewModel(it.repoManager) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberTransformingLazyColumnState()
    // Kept here, so the address is still there after "Try again".
    val address = rememberTextFieldState()
    LaunchedEffect(state) {
        if (state is Added) onAdded()
    }

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.add_repository)) } }
            when (val current = state) {
                None -> addressInput(address, onLoad = viewModel::load)
                is Fetching ->
                    if (current.done) {
                        preview(current, onAdd = viewModel::add, onCancel = viewModel::reset)
                    } else {
                        item { Progress(stringResource(R.string.add_repository_loading)) }
                    }
                Adding, is Added -> item { Progress(stringResource(R.string.add_repository_adding)) }
                is AddRepoError -> {
                    item { CenteredText(errorText(current.errorType)) }
                    item { WideButton(stringResource(R.string.install_retry), onClick = viewModel::reset) }
                }
            }
        }
    }
}

private fun TransformingLazyColumnScope.addressInput(
    address: TextFieldState,
    onLoad: (String) -> Unit,
) {
    item {
        TextInput(
            state = address,
            hint = stringResource(R.string.add_repository_hint),
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done,
                ),
        )
    }
    item {
        WideButton(
            stringResource(R.string.add_repository_load),
            onClick = { onLoad(address.text.toString()) },
            enabled = address.text.isNotBlank(),
        )
    }
    item { CenteredText(stringResource(R.string.add_repository_help), small = true) }
}

private fun TransformingLazyColumnScope.preview(
    fetching: Fetching,
    onAdd: () -> Unit,
    onCancel: () -> Unit,
) {
    val repo = fetching.receivedRepo ?: return
    item { CenteredText(repo.getName(LocaleListCompat.getDefault()) ?: repo.address) }
    item { CenteredText(repo.address, small = true) }
    item {
        val fingerprint = formatFingerprint(repo.fingerprint)
        CenteredText(stringResource(R.string.add_repository_fingerprint, fingerprint), small = true)
    }
    val result = fetching.fetchResult
    val isNew = result is FetchResult.IsNewRepository || result is FetchResult.IsNewRepoAndNewMirror
    if (isNew) {
        item { CenteredText(stringResource(R.string.add_repository_compare), small = true) }
        item { WideButton(stringResource(R.string.add_repository_add), onClick = onAdd) }
    } else {
        item { CenteredText(stringResource(R.string.add_repository_exists)) }
    }
    item { WideButton(stringResource(R.string.cancel), onClick = onCancel, tonal = true) }
}

/** Small groups of characters, so the user can compare the fingerprint piece by piece. */
private fun formatFingerprint(fingerprint: String): String {
    val groups = fingerprint.uppercase().chunked(FINGERPRINT_GROUP_SIZE)
    return groups.joinToString(" ")
}

private const val FINGERPRINT_GROUP_SIZE = 4

@Composable
private fun errorText(type: ErrorType): String =
    stringResource(
        when (type) {
            ErrorType.UNKNOWN_SOURCES_DISALLOWED -> R.string.add_repository_error_disallowed
            ErrorType.INVALID_FINGERPRINT -> R.string.add_repository_error_fingerprint
            ErrorType.IS_ARCHIVE_REPO -> R.string.add_repository_error_archive
            ErrorType.INVALID_INDEX -> R.string.add_repository_error_index
            ErrorType.IO_ERROR -> R.string.add_repository_error_io
        },
    )

@Composable
private fun Progress(label: String) {
    Button(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        icon = { CircularProgressIndicator() },
        label = { Text(label) },
    )
}

@Composable
private fun WideButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tonal: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        colors = if (tonal) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.buttonColors(),
        label = { Text(label) },
    )
}
