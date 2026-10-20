package io.github.datend3nker.openwrist.install

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

/**
 * Installs APK files with [PackageInstaller] sessions, like the F-Droid
 * client, and uninstalls apps. Android shows its own confirmation for every
 * installation and uninstallation, so nothing happens without the user
 * agreeing.
 *
 * Installing needs the `REQUEST_INSTALL_PACKAGES` permission, which the user
 * has to allow once, see [canInstall]. Uninstalling needs
 * `REQUEST_DELETE_PACKAGES`, which Android grants without asking.
 */
class ApkInstaller(
    context: Context,
) {
    private val context = context.applicationContext
    private val installer = this.context.packageManager.packageInstaller

    /** False while the user has not allowed OpenWrist to install apps. */
    fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    /**
     * Installs [apkFile] and suspends until Android reports the result. Only
     * call this with an APK that passed [ApkVerifier].
     */
    suspend fun install(
        packageName: String,
        apkFile: File,
    ): InstallState {
        val sessionId = withContext(Dispatchers.IO) { writeSession(packageName, apkFile) }
        val status =
            awaitResult(
                requestId = sessionId,
                packageName = packageName,
                start = { installer.openSession(sessionId).use { session -> session.commit(it) } },
                onCancel = { installer.abandonSession(sessionId) },
            )
        return when (status) {
            PackageInstaller.STATUS_SUCCESS -> InstallState.Installed
            PackageInstaller.STATUS_FAILURE_ABORTED -> InstallState.Aborted
            else -> InstallState.Failed(InstallError.InstallerFailed(status))
        }
    }

    /**
     * Uninstalls [packageName] and suspends until Android reports the result.
     * Android asks the user to confirm first.
     */
    suspend fun uninstall(packageName: String): UninstallResult {
        val status =
            awaitResult(
                requestId = nextUninstallId.getAndDecrement(),
                packageName = packageName,
                start = { installer.uninstall(packageName, it) },
            )
        return when (status) {
            PackageInstaller.STATUS_SUCCESS -> UninstallResult.Uninstalled
            PackageInstaller.STATUS_FAILURE_ABORTED -> UninstallResult.Aborted
            else -> UninstallResult.Failed(status)
        }
    }

    /**
     * Calls [start] with an [IntentSender] for the system's result and
     * suspends until the final status arrives. Shows the system confirmation
     * when Android asks for it. [requestId] tells the results apart: session
     * IDs for installations, negative numbers for uninstallations.
     */
    private suspend fun awaitResult(
        requestId: Int,
        packageName: String,
        start: (IntentSender) -> Unit,
        onCancel: () -> Unit = {},
    ): Int =
        suspendCancellableCoroutine { continuation ->
            val receiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        receiverContext: Context,
                        intent: Intent,
                    ) {
                        if (intent.getIntExtra(EXTRA_REQUEST_ID, 0) != requestId) return
                        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, Int.MIN_VALUE)
                        Log.i(TAG, "Status for $packageName: $status")
                        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                            // Show the system confirmation. Another broadcast follows.
                            IntentCompat
                                .getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                ?.let { context.startActivity(it) }
                            return
                        }
                        context.unregisterReceiver(this)
                        if (continuation.isActive) continuation.resume(status)
                    }
                }
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(ACTION_RESULT),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            continuation.invokeOnCancellation {
                runCatching { context.unregisterReceiver(receiver) }
                runCatching { onCancel() }
            }
            start(resultIntent(requestId).intentSender)
        }

    private fun writeSession(
        packageName: String,
        apkFile: File,
    ): Int {
        val params =
            SessionParams(SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(packageName)
                setSize(apkFile.length())
                setInstallReason(PackageManager.INSTALL_REASON_USER)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
                }
            }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                apkFile.inputStream().use { it.copyTo(output) }
                session.fsync(output)
            }
        }
        return sessionId
    }

    /** The system adds the result to this intent, so it has to be mutable. */
    private fun resultIntent(requestId: Int): PendingIntent {
        val intent =
            Intent(ACTION_RESULT).apply {
                setPackage(context.packageName)
                putExtra(EXTRA_REQUEST_ID, requestId)
            }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        return PendingIntent.getBroadcast(context, requestId, intent, flags)
    }

    private companion object {
        const val TAG = "ApkInstaller"
        const val ACTION_RESULT = "io.github.datend3nker.openwrist.install.RESULT"
        const val EXTRA_REQUEST_ID = "io.github.datend3nker.openwrist.install.REQUEST_ID"

        /** Session IDs are positive, so uninstallations count down from -1. */
        val nextUninstallId = AtomicInteger(-1)
    }
}
