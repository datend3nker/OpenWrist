package io.github.datend3nker.openwrist.install

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fdroid.database.AppVersion
import org.fdroid.database.Repository
import org.fdroid.download.DownloaderFactory
import java.io.File
import java.io.IOException

/**
 * Downloads, verifies and installs app versions, one flow per package.
 *
 * The state of each installation is kept in [states] by package name, so a
 * screen shows it again after the user left and came back.
 */
class InstallManager(
    context: Context,
    private val scope: CoroutineScope,
    private val downloaderFactory: DownloaderFactory,
    private val verifier: ApkVerifier,
    private val installer: ApkInstaller,
) {
    private val apkDir = File(context.cacheDir, "apks")

    private val mutableStates = MutableStateFlow<Map<String, InstallState>>(emptyMap())
    val states: StateFlow<Map<String, InstallState>> = mutableStates.asStateFlow()

    /** Starts installing [version] from [repo]. Does nothing while one is running for the package. */
    fun install(
        repo: Repository,
        version: AppVersion,
    ) {
        val packageName = version.packageName
        when (mutableStates.value[packageName]) {
            is InstallState.Downloading, InstallState.Verifying, InstallState.Installing -> return
            else -> setState(packageName, InstallState.Downloading(progress = null))
        }
        scope.launch {
            val apkFile = File(apkDir, "${version.file.sha256}.apk")
            val state =
                try {
                    downloadVerifyInstall(repo, version, apkFile)
                } finally {
                    apkFile.delete()
                }
            setState(packageName, state)
        }
    }

    /** Forgets a final state, for example after the user saw the error. */
    fun clearState(packageName: String) {
        mutableStates.update { it - packageName }
    }

    private suspend fun downloadVerifyInstall(
        repo: Repository,
        version: AppVersion,
        apkFile: File,
    ): InstallState {
        val error =
            if (download(repo, version, apkFile)) {
                verify(version, apkFile)?.let { InstallError.VerificationFailed(it) }
            } else {
                InstallError.DownloadFailed
            }
        return if (error == null) install(version, apkFile) else InstallState.Failed(error)
    }

    private suspend fun verify(
        version: AppVersion,
        apkFile: File,
    ): VerificationError? {
        setState(version.packageName, InstallState.Verifying)
        val error = withContext(Dispatchers.IO) { verifier.verify(apkFile, expectedApk(version)) }
        if (error != null) Log.w(TAG, "Rejected ${version.packageName}: $error")
        return error
    }

    private suspend fun install(
        version: AppVersion,
        apkFile: File,
    ): InstallState {
        setState(version.packageName, InstallState.Installing)
        return try {
            installer.install(version.packageName, apkFile)
        } catch (e: IOException) {
            Log.w(TAG, "Installing ${version.packageName} failed", e)
            InstallState.Failed(InstallError.InstallerUnavailable)
        } catch (e: SecurityException) {
            // For example when the install permission was taken back meanwhile.
            Log.w(TAG, "Installing ${version.packageName} failed", e)
            InstallState.Failed(InstallError.InstallerUnavailable)
        }
    }

    /**
     * Returns false if the download failed. The downloader tries every mirror
     * and also checks the SHA-256 hash from the index while downloading.
     */
    @Suppress("TooGenericExceptionCaught") // the library throws IOException, Ktor and its own exceptions
    private suspend fun download(
        repo: Repository,
        version: AppVersion,
        apkFile: File,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                startDownload(repo, version, apkFile)
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Downloading ${version.packageName} failed", e)
                false
            }
        }

    private fun startDownload(
        repo: Repository,
        version: AppVersion,
        apkFile: File,
    ) {
        apkDir.mkdirs()
        val uri =
            repo.address
                .toUri()
                .buildUpon()
                .apply {
                    version.file.name
                        .split("/")
                        .filter { it.isNotEmpty() }
                        .forEach { appendPath(it) }
                }.build()
        val downloader = downloaderFactory.create(repo, uri, version.file, apkFile)
        downloader.setListener { bytesRead, totalBytes ->
            val progress = if (totalBytes > 0) bytesRead.toFloat() / totalBytes else null
            setState(version.packageName, InstallState.Downloading(progress))
        }
        downloader.download()
    }

    private fun setState(
        packageName: String,
        state: InstallState,
    ) {
        mutableStates.update { it + (packageName to state) }
    }

    private companion object {
        const val TAG = "InstallManager"
    }
}

/** What the signed index says about the APK of [version]. */
internal fun expectedApk(version: AppVersion): ExpectedApk =
    ExpectedApk(
        packageName = version.packageName,
        versionCode = version.versionCode,
        sha256 = version.file.sha256,
        signerSha256 =
            version.manifest.signer
                ?.sha256
                .orEmpty()
                .toSet(),
    )
