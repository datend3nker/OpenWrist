package io.github.datend3nker.openwrist.repo

import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.fdroid.CompatibilityCheckerImpl
import org.fdroid.index.v2.PackageManifest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchCompatibilityCheckerTest {
    /** A watch on SDK 34 with an arm64 CPU. */
    private val checker =
        WatchCompatibilityChecker(
            CompatibilityCheckerImpl(
                packageManager = watchPackageManager(),
                sdkInt = 34,
                supportedAbis = arrayOf("arm64-v8a"),
            ),
        )

    @Test
    fun `accepts a watch build`() {
        assertTrue(checker.isCompatible(manifest(features = listOf(FEATURE_WATCH))))
    }

    @Test
    fun `rejects a phone build without the watch feature`() {
        assertFalse(checker.isCompatible(manifest(features = emptyList())))
    }

    @Test
    fun `rejects a version without feature list`() {
        assertFalse(checker.isCompatible(manifest(features = null)))
    }

    @Test
    fun `accepts a watch build with native code for the watch ABI`() {
        val manifest = manifest(features = listOf(FEATURE_WATCH), nativecode = listOf("armeabi-v7a", "arm64-v8a"))
        assertTrue(checker.isCompatible(manifest))
    }

    @Test
    fun `rejects a watch build with native code for other ABIs only`() {
        val manifest = manifest(features = listOf(FEATURE_WATCH), nativecode = listOf("armeabi-v7a", "x86_64"))
        assertFalse(checker.isCompatible(manifest))
    }

    @Test
    fun `rejects a watch build that needs a newer SDK`() {
        assertFalse(checker.isCompatible(manifest(features = listOf(FEATURE_WATCH), minSdkVersion = 35)))
    }

    @Test
    fun `rejects a watch build that needs a feature the watch lacks`() {
        val manifest = manifest(features = listOf(FEATURE_WATCH, "android.hardware.telephony"))
        assertFalse(checker.isCompatible(manifest))
    }

    private fun watchPackageManager(): PackageManager =
        mockk {
            every { systemAvailableFeatures } returns arrayOf(FeatureInfo().apply { name = FEATURE_WATCH })
        }

    private fun manifest(
        features: List<String>?,
        nativecode: List<String>? = null,
        minSdkVersion: Int? = 30,
    ): PackageManifest =
        object : PackageManifest {
            override val minSdkVersion: Int? = minSdkVersion
            override val maxSdkVersion: Int? = null
            override val featureNames: List<String>? = features
            override val nativecode: List<String>? = nativecode
            override val targetSdkVersion: Int? = 34
        }
}
