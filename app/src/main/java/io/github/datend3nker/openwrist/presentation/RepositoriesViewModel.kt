package io.github.datend3nker.openwrist.presentation

import androidx.lifecycle.ViewModel
import io.github.datend3nker.openwrist.repo.RepoRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.fdroid.database.Repository
import org.fdroid.index.RepoManager

class RepositoriesViewModel(
    private val repoManager: RepoManager,
    private val repoRefresher: RepoRefresher,
    private val appScope: CoroutineScope,
) : ViewModel() {
    /** All repositories in the database, enabled or not. */
    val repositories: StateFlow<List<Repository>> = repoManager.repositoriesState

    /**
     * Enables or disables [repository]. Apps of disabled repositories are no
     * longer listed. An enabled repository may have no index yet, so it is
     * refreshed right away; in the app scope, so leaving the screen does not
     * stop it.
     */
    fun setEnabled(
        repository: Repository,
        enabled: Boolean,
    ) {
        appScope.launch(Dispatchers.IO) {
            repoManager.setRepositoryEnabled(repository.repoId, enabled)
            if (enabled) repoRefresher.refresh()
        }
    }
}
