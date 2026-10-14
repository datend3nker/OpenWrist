package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.datend3nker.openwrist.R
import io.github.datend3nker.openwrist.install.InstallState
import io.github.datend3nker.openwrist.repo.AppDetails

@Composable
fun AppDetailsScreen(packageName: String) {
    val context = LocalContext.current
    val viewModel =
        containerViewModel(key = packageName) {
            AppDetailsViewModel(
                it.watchApps,
                it.installManager,
                it.apkInstaller,
                context.packageManager,
                context.packageName,
                packageName,
            )
        }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val installState by viewModel.installState.collectAsStateWithLifecycle()
    val canInstall by viewModel.canInstall.collectAsStateWithLifecycle()
    val uninstalling by viewModel.uninstalling.collectAsStateWithLifecycle()
    val uninstallError by viewModel.uninstallError.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.checkCanInstall()
        onPauseOrDispose {}
    }
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            when (val current = state) {
                DetailsState.Loading -> item { CircularProgressIndicator() }
                DetailsState.NotFound ->
                    item { CenteredText(stringResource(R.string.details_not_found)) }
                is DetailsState.Loaded -> {
                    val details = current.details
                    item { AppIcon(details.repoId, details.icon, DetailsIconSize) }
                    item { ListHeader { Text(details.name, textAlign = TextAlign.Center) } }
                    details.summary?.let { item { CenteredText(it) } }
                    item { VersionText(details) }
                    item { DetailsActions(details, current.otherStoreName, viewModel, installState, canInstall) }
                    if (details.installedVersionName != null) {
                        item { UninstallButton(uninstalling, uninstallError, onUninstall = viewModel::uninstall) }
                    }
                    details.authorName?.let { item { CenteredText(stringResource(R.string.details_author, it)) } }
                    details.license?.let { item { CenteredText(stringResource(R.string.details_license, it)) } }
                    details.description?.let { html ->
                        item {
                            Text(
                                HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim(),
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    item { CenteredText(details.packageName, small = true) }
                }
            }
        }
    }
}

private val DetailsIconSize = 48.dp

/**
 * Install or update, the permission hint if needed, and opening the installed
 * app. An app that another store installed is only updated after the user
 * agreed in a dialog.
 */
@Composable
private fun DetailsActions(
    details: AppDetails,
    otherStoreName: String?,
    viewModel: AppDetailsViewModel,
    installState: InstallState?,
    canInstall: Boolean,
) {
    val context = LocalContext.current
    val isUpdate = details.installedVersionName != null
    var askOtherStore by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (details.suggestedVersion != null) {
            if (canInstall) {
                InstallButton(
                    state = installState,
                    isUpdate = isUpdate,
                    onInstall = {
                        if (isUpdate && otherStoreName != null) askOtherStore = true else viewModel.install()
                    },
                    onRetry = {
                        viewModel.clearInstallState()
                        viewModel.install()
                    },
                )
            } else {
                InstallPermissionHint()
            }
        }
        val launchIntent = if (details.installedVersionName != null) viewModel.launchIntent() else null
        if (launchIntent != null) {
            Button(
                onClick = { context.startActivity(launchIntent) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text(stringResource(R.string.open)) },
            )
        }
    }
    if (otherStoreName != null) {
        UpdateFromOtherStoreDialog(
            visible = askOtherStore,
            otherStoreName = otherStoreName,
            onConfirm = {
                askOtherStore = false
                viewModel.install()
            },
            onDismiss = { askOtherStore = false },
        )
    }
}

/** Starts Android's uninstaller, which asks the user to confirm. */
@Composable
private fun UninstallButton(
    uninstalling: Boolean,
    errorStatus: Int?,
    onUninstall: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedButton(
            onClick = onUninstall,
            enabled = !uninstalling,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.uninstall)) },
        )
        if (errorStatus != null) CenteredText(stringResource(R.string.uninstall_error, errorStatus), small = true)
    }
}

@Composable
private fun UpdateFromOtherStoreDialog(
    visible: Boolean,
    otherStoreName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = onConfirm) },
        title = { Text(stringResource(R.string.update_other_store_title)) },
        text = { Text(stringResource(R.string.update_other_store_text, otherStoreName)) },
    )
}

@Composable
private fun VersionText(details: AppDetails) {
    val installed = details.installedVersionName
    val suggested = details.suggestedVersion?.versionName
    val text =
        when {
            installed != null && suggested != null ->
                stringResource(R.string.details_update_available, installed, suggested)
            installed != null -> stringResource(R.string.details_installed, installed)
            suggested != null -> stringResource(R.string.details_version, suggested)
            else -> stringResource(R.string.details_no_compatible_version)
        }
    CenteredText(text)
}

@Composable
private fun CenteredText(
    text: String,
    small: Boolean = false,
) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
    )
}
