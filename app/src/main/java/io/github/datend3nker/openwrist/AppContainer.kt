package io.github.datend3nker.openwrist

import android.content.Context
import io.github.datend3nker.openwrist.repo.DefaultRepositories
import io.github.datend3nker.openwrist.repo.RepoDownloaderFactory
import io.github.datend3nker.openwrist.repo.RepoRefresher
import io.github.datend3nker.openwrist.repo.WatchApps
import io.github.datend3nker.openwrist.repo.WatchCompatibilityChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.fdroid.database.FDroidDatabase
import org.fdroid.database.FDroidDatabaseHolder
import org.fdroid.download.HttpManager
import org.fdroid.index.RepoUpdater

/**
 * Creates the objects that live as long as the app and wires them together.
 * Plain constructor calls instead of a dependency injection framework, so
 * the wiring stays in one readable place.
 */
class AppContainer(
    context: Context,
) {
    private val appContext = context.applicationContext

    /** For work that must go on when the user leaves a screen, such as a refresh. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val db: FDroidDatabase by lazy {
        FDroidDatabaseHolder.getDb(appContext, name = "openwrist", fixture = DefaultRepositories())
    }

    /** Must be the same for storing the index and for reading it, see [WatchApps]. */
    private val compatibilityChecker = WatchCompatibilityChecker(appContext.packageManager)

    private val httpManager by lazy { HttpManager(userAgent = "OpenWrist ${BuildConfig.VERSION_NAME}") }

    private val downloaderFactory by lazy { RepoDownloaderFactory(httpManager) }

    val repoRefresher: RepoRefresher by lazy {
        RepoRefresher(db) { listener ->
            RepoUpdater(
                tempDir = appContext.cacheDir,
                db = db,
                downloaderFactory = downloaderFactory,
                compatibilityChecker = compatibilityChecker,
                listener = listener,
            )
        }
    }

    val watchApps: WatchApps by lazy { WatchApps(db, appContext.packageManager, compatibilityChecker) }
}
