/**
 * Repository logic of OpenWrist: downloads and stores the index of F-Droid
 * compatible repositories with the F-Droid libraries and answers which watch
 * apps can be installed. No user interface and no user-visible text.
 */
package io.github.datend3nker.openwrist.repo

import android.content.pm.PackageManager
import org.fdroid.CompatibilityChecker
import org.fdroid.CompatibilityCheckerImpl
import org.fdroid.index.v2.PackageManifest

/** The feature every Wear OS app declares in its manifest. */
const val FEATURE_WATCH = "android.hardware.type.watch"

/**
 * Accepts only versions that are built for watches and run on this watch.
 *
 * [CompatibilityCheckerImpl] already checks the SDK version, the native code
 * ABI and the required features. It only rejects versions that need a feature
 * the device is missing, so it accepts phone builds, which declare no watch
 * feature. When phone and watch builds share one application ID, the phone
 * build usually has the higher version code and would be offered to the
 * watch. This checker therefore also requires [FEATURE_WATCH].
 *
 * Use it everywhere the F-Droid libraries take a [CompatibilityChecker], so
 * the `isCompatible` flag in the database and the update checks agree.
 */
class WatchCompatibilityChecker(
    private val delegate: CompatibilityChecker,
) : CompatibilityChecker {
    constructor(packageManager: PackageManager) : this(CompatibilityCheckerImpl(packageManager))

    override fun isCompatible(manifest: PackageManifest): Boolean {
        val features = manifest.featureNames ?: return false
        return FEATURE_WATCH in features && delegate.isCompatible(manifest)
    }
}
