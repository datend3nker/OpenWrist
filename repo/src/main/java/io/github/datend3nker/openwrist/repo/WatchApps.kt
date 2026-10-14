package io.github.datend3nker.openwrist.repo

import android.content.pm.PackageManager
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.asFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.fdroid.database.AppListItem
import org.fdroid.database.AppListSortOrder
import org.fdroid.database.AppVersion
import org.fdroid.database.DbUpdateChecker
import org.fdroid.database.FDroidDatabase
import org.fdroid.database.Repository
import org.fdroid.database.UpdatableApp
import org.fdroid.index.v2.FileV2

/** Everything the details screen shows about one app. */
data class AppDetails(
    val packageName: String,
    /** The repository the metadata and [icon] come from. */
    val repoId: Long,
    val name: String,
    /** Load it with [AppIcons]. */
    val icon: FileV2?,
    val summary: String?,
    /** Formatted as HTML, as in the index. */
    val description: String?,
    val authorName: String?,
    val license: String?,
    val webSite: String?,
    val sourceCode: String?,
    /** The installed version name, or null if the app is not installed. */
    val installedVersionName: String?,
    /** The watch build to install or update to, or null if there is none. */
    val suggestedVersion: AppVersion?,
    /** The repository [suggestedVersion] comes from. */
    val repository: Repository?,
)

/**
 * Answers which watch apps the enabled repositories offer.
 *
 * Only apps with at least one version accepted by [WatchCompatibilityChecker]
 * are listed. The database stores that as `isCompatible` when the index is
 * downloaded, so the checker given here must be the same one the
 * [org.fdroid.index.RepoUpdater] uses.
 */
class WatchApps(
    private val db: FDroidDatabase,
    private val packageManager: PackageManager,
    compatibilityChecker: WatchCompatibilityChecker,
) {
    private val updateChecker = DbUpdateChecker(db, packageManager, compatibilityChecker)

    /** All watch apps, most recently updated first. */
    fun appList(): Flow<List<AppListItem>> = query(searchQuery = null)

    /**
     * Watch apps matching [searchQuery] in name, summary, description, author
     * or package name, using the database's full text search.
     */
    fun search(searchQuery: String): Flow<List<AppListItem>> = query(searchQuery)

    private fun query(searchQuery: String?): Flow<List<AppListItem>> =
        db
            .getAppDao()
            .getAppListItems(packageManager, searchQuery, AppListSortOrder.LAST_UPDATED)
            .asFlow()
            .map { items -> items.filter { it.isCompatible } }
            .flowOn(Dispatchers.Default)

    /**
     * Installed apps for which a newer watch build exists. Only versions
     * signed with the installed app's key count, so an update never fails
     * because of a different signer.
     */
    suspend fun updates(): List<UpdatableApp> =
        withContext(Dispatchers.IO) {
            updateChecker.getUpdatableApps().sortedBy { (it.name ?: it.packageName).lowercase() }
        }

    /** Returns null if no enabled repository has an app with [packageName]. */
    suspend fun details(packageName: String): AppDetails? =
        withContext(Dispatchers.IO) {
            val appDao = db.getAppDao()
            val app =
                appDao.getRepositoryIdsForApp(packageName).firstNotNullOfOrNull { repoId ->
                    appDao.getApp(repoId, packageName)
                } ?: return@withContext null
            val suggestedVersion = suggestedVersion(packageName, app.metadata.preferredSigner)
            AppDetails(
                packageName = packageName,
                repoId = app.repoId,
                name = app.name ?: packageName,
                icon = app.getIcon(LocaleListCompat.getDefault()),
                summary = app.summary,
                description = app.getDescription(LocaleListCompat.getDefault()),
                authorName = app.authorName,
                license = app.metadata.license,
                webSite = app.metadata.webSite,
                sourceCode = app.metadata.sourceCode,
                installedVersionName = installedVersionName(packageName),
                suggestedVersion = suggestedVersion,
                repository = suggestedVersion?.let { db.getRepositoryDao().getRepository(it.repoId) },
            )
        }

    /**
     * The repository picks a preferred signer per app, not per version. When
     * phone and watch builds share an application ID, it may belong to the
     * phone builds only (Catima: phone builds signed by F-Droid, watch builds
     * by the developer). For new installs we then fall back to any signer.
     * For installed apps [DbUpdateChecker] only accepts the installed signer,
     * and Android rejects updates with another signer anyway.
     */
    private fun suggestedVersion(
        packageName: String,
        preferredSigner: String?,
    ): AppVersion? =
        updateChecker.getSuggestedVersion(packageName, preferredSigner)
            ?: updateChecker.getSuggestedVersion(packageName, preferredSigner = null)

    private fun installedVersionName(packageName: String): String? =
        try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
}
