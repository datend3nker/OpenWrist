/**
 * Installation logic of OpenWrist: downloads APKs, verifies them against the
 * signed repository index and installs them with the system installer. No
 * user interface and no user-visible text.
 */
package io.github.datend3nker.openwrist.install

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import java.io.File
import java.security.MessageDigest

/** Why a downloaded APK was rejected. */
sealed interface VerificationError {
    /** The file's SHA-256 hash differs from the one in the index. */
    data object WrongChecksum : VerificationError

    /** Android could not read the file as an APK. */
    data object NotAnApk : VerificationError

    data class WrongPackage(
        val expected: String,
        val actual: String,
    ) : VerificationError

    data class WrongVersion(
        val expected: Long,
        val actual: Long,
    ) : VerificationError

    /** The index names no signer for this version, so there is nothing to compare with. */
    data object UnknownSigner : VerificationError

    /** The APK is signed with other keys than the index says. */
    data object WrongSigner : VerificationError
}

/** What the signed index says about an APK. */
data class ExpectedApk(
    val packageName: String,
    val versionCode: Long,
    /** Lowercase hex SHA-256 of the whole file. */
    val sha256: String,
    /** Lowercase hex SHA-256 of each signing certificate, empty if the index has none. */
    val signerSha256: Set<String>,
)

/** What a downloaded APK actually contains. */
data class ApkInfo(
    val packageName: String,
    val versionCode: Long,
    /** Lowercase hex SHA-256 of each signing certificate. */
    val signerSha256: Set<String>,
)

/**
 * Checks a downloaded APK against the signed index before it is installed:
 * file checksum, package name, version code and signing certificates.
 *
 * The F-Droid downloader already checks the checksum while downloading. It is
 * checked here again on purpose, so the install does not depend on how the
 * library behaves.
 */
class ApkVerifier(
    private val packageManager: PackageManager,
) {
    /** Returns null if [apkFile] may be installed. */
    fun verify(
        apkFile: File,
        expected: ExpectedApk,
    ): VerificationError? {
        if (sha256(apkFile) != expected.sha256.lowercase()) return VerificationError.WrongChecksum
        val info = readApkInfo(apkFile)
        return if (info == null) VerificationError.NotAnApk else compare(expected, info)
    }

    private fun readApkInfo(apkFile: File): ApkInfo? {
        val packageInfo =
            packageManager.getPackageArchiveInfo(apkFile.path, PackageManager.GET_SIGNING_CERTIFICATES)
                ?: return null
        return ApkInfo(
            packageName = packageInfo.packageName,
            versionCode = packageInfo.longVersionCode,
            signerSha256 = signerSha256(packageInfo),
        )
    }

    /**
     * The certificates the APK's contents are signed with. With key rotation
     * this is the current key, which is also what the index lists.
     */
    private fun signerSha256(packageInfo: PackageInfo): Set<String> =
        packageInfo.signingInfo
            ?.apkContentsSigners
            .orEmpty()
            .map { sha256(it.toByteArray()) }
            .toSet()
}

/** The checks that do not need Android, separate so they can be unit tested. */
internal fun compare(
    expected: ExpectedApk,
    actual: ApkInfo,
): VerificationError? =
    when {
        actual.packageName != expected.packageName ->
            VerificationError.WrongPackage(expected.packageName, actual.packageName)
        actual.versionCode != expected.versionCode ->
            VerificationError.WrongVersion(expected.versionCode, actual.versionCode)
        expected.signerSha256.isEmpty() -> VerificationError.UnknownSigner
        actual.signerSha256 != expected.signerSha256.map { it.lowercase() }.toSet() ->
            VerificationError.WrongSigner
        else -> null
    }

internal fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().toHex()
}

internal fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
