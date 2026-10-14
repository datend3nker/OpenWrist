package io.github.datend3nker.openwrist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.datend3nker.openwrist.repo.DefaultRepositories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.fdroid.database.Repository
import org.fdroid.index.RepoManager

class RepositoryDetailsViewModel(
    private val repoManager: RepoManager,
    private val appScope: CoroutineScope,
    private val repoId: Long,
) : ViewModel() {
    /** The repository, or null once it was removed. */
    val repository: StateFlow<Repository?> =
        repoManager.repositoriesState
            .map { list -> list.find { it.repoId == repoId } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, repoManager.getRepository(repoId))

    /**
     * Only repositories the user added can be removed. Archives go together
     * with their main repository, so they cannot be removed on their own.
     */
    fun canRemove(repository: Repository): Boolean {
        val isDefault = DefaultRepositories.isDefault(repository.address)
        return !isDefault && !repository.isArchiveRepo
    }

    /**
     * Removes the repository and its apps from the database. Apps installed
     * from it stay installed. Runs in the app scope, so leaving the screen
     * right after does not stop it.
     */
    fun remove() {
        appScope.launch(Dispatchers.IO) { repoManager.deleteRepository(repoId) }
    }
}
