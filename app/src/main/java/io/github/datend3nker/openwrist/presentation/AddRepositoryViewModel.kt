package io.github.datend3nker.openwrist.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.fdroid.index.RepoManager
import org.fdroid.repo.AddRepoError

/**
 * Adds a repository in two steps, as the F-Droid library does it: first
 * the index is downloaded and shown as a preview, then the user decides
 * whether to add it.
 */
class AddRepositoryViewModel(
    private val repoManager: RepoManager,
) : ViewModel() {
    val state = repoManager.addRepoState

    init {
        // The F-Droid library logs nothing on Android, so keep the reason of a failure.
        viewModelScope.launch {
            state.collect { state ->
                if (state is AddRepoError) Log.w(TAG, "Adding a repository failed: ${state.errorType}", state.exception)
            }
        }
    }

    /**
     * Downloads the index at [address]. If the address carries a
     * `fingerprint` parameter, the index must be signed with that key.
     */
    fun load(address: String) = repoManager.fetchRepositoryPreview(address.trim())

    /** Only valid while the preview is shown. */
    fun add() = repoManager.addFetchedRepository()

    /** Back to entering an address. */
    fun reset() = repoManager.abortAddingRepository()

    // The library keeps the state, so a later visit would see this one.
    override fun onCleared() = repoManager.abortAddingRepository()

    private companion object {
        const val TAG = "AddRepository"
    }
}
