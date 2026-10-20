package io.github.datend3nker.openwrist.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import io.github.datend3nker.openwrist.R
import io.github.datend3nker.openwrist.install.InstallError
import io.github.datend3nker.openwrist.install.InstallState

/**
 * The install or update button of the details screen, with the progress of a
 * running installation and the reason of a failed one.
 */
@Composable
fun InstallButton(
    state: InstallState?,
    isUpdate: Boolean,
    onInstall: () -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        is InstallState.Downloading -> ProgressButton(stringResource(R.string.install_downloading), state.progress)
        InstallState.Verifying -> ProgressButton(stringResource(R.string.install_verifying), progress = null)
        InstallState.Installing -> ProgressButton(stringResource(R.string.install_installing), progress = null)
        is InstallState.Failed ->
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(),
                label = { Text(stringResource(R.string.install_retry)) },
                secondaryLabel = {
                    Text(installErrorText(state.error), maxLines = 3, overflow = TextOverflow.Ellipsis)
                },
            )
        InstallState.Installed, InstallState.Aborted, null ->
            Button(
                onClick = onInstall,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(if (isUpdate) R.string.install_update else R.string.install)) },
            )
    }
}

@Composable
private fun ProgressButton(
    label: String,
    progress: Float?,
) {
    Button(
        onClick = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        icon = {
            val size = Modifier.size(ButtonDefaults.IconSize)
            if (progress == null) {
                CircularProgressIndicator(modifier = size)
            } else {
                CircularProgressIndicator(progress = { progress }, modifier = size)
            }
        },
        label = { Text(label) },
    )
}

@Composable
private fun installErrorText(error: InstallError): String =
    when (error) {
        InstallError.DownloadFailed -> stringResource(R.string.install_error_download)
        is InstallError.VerificationFailed -> stringResource(R.string.install_error_verification)
        InstallError.InstallerUnavailable -> stringResource(R.string.install_error_installer)
        is InstallError.InstallerFailed ->
            when (error.status) {
                PackageInstaller.STATUS_FAILURE_CONFLICT -> stringResource(R.string.install_error_conflict)
                PackageInstaller.STATUS_FAILURE_STORAGE -> stringResource(R.string.install_error_storage)
                PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> stringResource(R.string.install_error_incompatible)
                else -> stringResource(R.string.install_error_status, error.status)
            }
    }

/**
 * Asks the user to allow OpenWrist to install apps. Opens the system setting
 * for it. Some watches have no such setting; then the permission can only
 * be granted over ADB, and the command is shown.
 */
@Composable
fun InstallPermissionHint() {
    val context = LocalContext.current
    var settingMissing by rememberSaveable { mutableStateOf(false) }
    Button(
        onClick = { settingMissing = !openInstallSetting(context) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.filledTonalButtonColors(),
        label = { Text(stringResource(R.string.install_permission_needed)) },
        secondaryLabel = { Text(stringResource(R.string.install_permission_open_settings)) },
    )
    if (settingMissing) {
        Text(
            stringResource(R.string.install_permission_adb, adbCommand(context.packageName)),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun adbCommand(packageName: String) = "adb shell appops set $packageName REQUEST_INSTALL_PACKAGES allow"

/** Returns false if the watch has no settings screen for the permission. */
private fun openInstallSetting(context: Context): Boolean =
    try {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri())
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
