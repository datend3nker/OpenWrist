package io.github.datend3nker.openwrist.install

/** The state of an installation started by OpenWrist. */
sealed interface InstallState {
    /** [progress] is between 0 and 1, or null if the size is unknown. */
    data class Downloading(
        val progress: Float?,
    ) : InstallState

    /** Checking the downloaded APK against the index, see [ApkVerifier]. */
    data object Verifying : InstallState

    /** Waiting for the system installer, which first asks the user to confirm. */
    data object Installing : InstallState

    data object Installed : InstallState

    /** The user declined the system confirmation. */
    data object Aborted : InstallState

    data class Failed(
        val error: InstallError,
    ) : InstallState
}

/** Why an installation failed. */
sealed interface InstallError {
    /** The APK could not be downloaded from any mirror, or its hash was wrong. */
    data object DownloadFailed : InstallError

    data class VerificationFailed(
        val error: VerificationError,
    ) : InstallError

    /** The system installer could not be started, for example without the install permission. */
    data object InstallerUnavailable : InstallError

    /**
     * The system installer refused the APK. [status] is one of the
     * `PackageInstaller.STATUS_FAILURE*` constants.
     */
    data class InstallerFailed(
        val status: Int,
    ) : InstallError
}
