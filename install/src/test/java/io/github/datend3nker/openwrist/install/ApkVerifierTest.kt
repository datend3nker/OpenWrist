package io.github.datend3nker.openwrist.install

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

/**
 * The checks that protect against tampered or mixed-up APKs (E6.6). Reading
 * an APK needs Android, so these tests use [compare] with prepared values.
 */
class ApkVerifierTest {
    private val expected =
        ExpectedApk(
            packageName = "org.example.watch",
            versionCode = 201,
            sha256 = "ignored here",
            signerSha256 = setOf(SIGNER_A),
        )
    private val matching = ApkInfo(packageName = "org.example.watch", versionCode = 201, signerSha256 = setOf(SIGNER_A))

    @Test
    fun `accepts the APK the index describes`() {
        assertNull(compare(expected, matching))
    }

    @Test
    fun `compares signers case-insensitively`() {
        assertNull(compare(expected.copy(signerSha256 = setOf(SIGNER_A.uppercase())), matching))
    }

    @Test
    fun `rejects an APK with another package name`() {
        val error = compare(expected, matching.copy(packageName = "org.example.evil"))
        assertEquals(VerificationError.WrongPackage("org.example.watch", "org.example.evil"), error)
    }

    @Test
    fun `rejects an APK with another version code`() {
        assertEquals(VerificationError.WrongVersion(201, 1004), compare(expected, matching.copy(versionCode = 1004)))
    }

    @Test
    fun `rejects an APK signed with another key`() {
        assertEquals(VerificationError.WrongSigner, compare(expected, matching.copy(signerSha256 = setOf(SIGNER_B))))
    }

    @Test
    fun `rejects an unsigned APK`() {
        assertEquals(VerificationError.WrongSigner, compare(expected, matching.copy(signerSha256 = emptySet())))
    }

    @Test
    fun `rejects an APK with an additional signer`() {
        val error = compare(expected, matching.copy(signerSha256 = setOf(SIGNER_A, SIGNER_B)))
        assertEquals(VerificationError.WrongSigner, error)
    }

    @Test
    fun `rejects a version whose signer the index does not name`() {
        assertEquals(VerificationError.UnknownSigner, compare(expected.copy(signerSha256 = emptySet()), matching))
    }

    @Test
    fun `computes the file checksum as lowercase hex`() {
        val file = File.createTempFile("apk", ".apk").apply { deleteOnExit() }
        file.writeText("abc")
        // SHA-256 of "abc" from FIPS 180-2.
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", sha256(file))
    }

    private companion object {
        const val SIGNER_A = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        const val SIGNER_B = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
    }
}
