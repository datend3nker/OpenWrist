package io.github.datend3nker.openwrist.repo

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.fdroid.database.FDroidDatabase
import org.fdroid.database.Repository
import org.fdroid.index.IndexUpdateListener
import org.fdroid.index.IndexUpdateResult
import org.fdroid.index.RepoUpdater

/** What [RepoRefresher] is doing right now. */
sealed interface RefreshState {
    data object Idle : RefreshState

    /** [progress] is between 0 and 1, or null if it is not known. */
    data class Running(
        val repoAddress: String,
        val progress: Float?,
    ) : RefreshState

    /** The last refresh failed for the repositories in [repoAddresses]. */
    data class Failed(
        val repoAddresses: List<String>,
    ) : RefreshState
}

/** How old the index may get before [RepoRefresher.isRefreshDue] asks for a new one. */
const val MAX_INDEX_AGE_MILLIS = 24 * 60 * 60 * 1000L

/**
 * Downloads the index of every enabled repository into the database, one
 * repository after another. [RepoUpdater] verifies the index signature
 * against the certificate stored for the repository.
 */
class RepoRefresher(
    private val db: FDroidDatabase,
    repoUpdaterFactory: (IndexUpdateListener) -> RepoUpdater,
) {
    private val mutableState = MutableStateFlow<RefreshState>(RefreshState.Idle)
    val state: StateFlow<RefreshState> = mutableState.asStateFlow()

    private val listener =
        object : IndexUpdateListener {
            override fun onDownloadProgress(
                repo: Repository,
                bytesRead: Long,
                totalBytes: Long,
            ) {
                mutableState.value = RefreshState.Running(repo.address, fraction(bytesRead, totalBytes))
            }

            override fun onUpdateProgress(
                repo: Repository,
                appsProcessed: Int,
                totalApps: Int,
            ) {
                mutableState.value =
                    RefreshState.Running(repo.address, fraction(appsProcessed.toLong(), totalApps.toLong()))
            }
        }
    private val repoUpdater = repoUpdaterFactory(listener)

    /**
     * True if an enabled repository was never downloaded or its last download
     * is older than [MAX_INDEX_AGE_MILLIS].
     */
    suspend fun isRefreshDue(now: Long = System.currentTimeMillis()): Boolean =
        withContext(Dispatchers.IO) {
            db.getRepositoryDao().getRepositories().any { repo ->
                val lastUpdated = repo.lastUpdated
                repo.enabled && (lastUpdated == null || now - lastUpdated > MAX_INDEX_AGE_MILLIS)
            }
        }

    /** Updates all enabled repositories. Does nothing while a refresh is running. */
    suspend fun refresh() {
        val current = mutableState.value
        if (current is RefreshState.Running) return
        if (!mutableState.compareAndSet(current, RefreshState.Running(repoAddress = "", progress = null))) return

        var failed = emptyList<String>()
        try {
            failed =
                withContext(Dispatchers.IO) {
                    db
                        .getRepositoryDao()
                        .getRepositories()
                        .filter { it.enabled }
                        .mapNotNull { repo -> repo.address.takeUnless { update(repo) } }
                }
        } finally {
            mutableState.value = if (failed.isEmpty()) RefreshState.Idle else RefreshState.Failed(failed)
        }
    }

    /** Returns false if the update failed. */
    private fun update(repo: Repository): Boolean {
        mutableState.value = RefreshState.Running(repo.address, progress = null)
        val result = repoUpdater.update(repo)
        if (result is IndexUpdateResult.Error) {
            Log.w(TAG, "Updating ${repo.address} failed", result.e)
            return false
        }
        Log.i(TAG, "Updated ${repo.address}: $result")
        return result != IndexUpdateResult.NotFound
    }

    private fun fraction(
        done: Long,
        total: Long,
    ): Float? = if (total > 0) done.toFloat() / total else null

    private companion object {
        const val TAG = "RepoRefresher"
    }
}
