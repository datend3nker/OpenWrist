/**
 * Wear OS user interface of OpenWrist: screens, navigation and the
 * translations of everything the user sees.
 */
package io.github.datend3nker.openwrist.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import io.github.datend3nker.openwrist.presentation.theme.OpenWristTheme

/** Entry point of the app on the watch. */
class MainActivity : ComponentActivity() {
    // The answer needs no handling: the update notification checks the
    // permission each time it would be shown.
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val openUpdates = intent.getBooleanExtra(EXTRA_OPEN_UPDATES, false)
        setContent {
            WearApp(openUpdates)
        }
        if (savedInstanceState == null) askForNotifications()
    }

    /** Since Android 13 the update notification needs the user's permission. */
    private fun askForNotifications() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(permission)
        }
    }

    companion object {
        /** Set by the update notification: start on the Updates screen. */
        const val EXTRA_OPEN_UPDATES = "io.github.datend3nker.openwrist.OPEN_UPDATES"
    }
}

/** The screens of the app. Each one is an entry on the back stack. */
sealed interface Screen {
    data object AppList : Screen

    data object Search : Screen

    data object Updates : Screen

    data object Repositories : Screen

    data object AddRepository : Screen

    data class RepositoryDetails(
        val repoId: Long,
    ) : Screen

    data class AppDetails(
        val packageName: String,
    ) : Screen
}

/**
 * Root composable. Swiping right goes back to the previous screen. With
 * [openUpdates] it starts on the Updates screen, above the app list.
 */
@Composable
fun WearApp(openUpdates: Boolean = false) {
    val backStack =
        remember {
            val start = if (openUpdates) listOf(Screen.AppList, Screen.Updates) else listOf(Screen.AppList)
            mutableStateListOf<Screen>().apply { addAll(start) }
        }
    OpenWristTheme {
        AppScaffold {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                sceneStrategies = listOf(rememberSwipeDismissableSceneStrategy()),
                entryProvider =
                    entryProvider {
                        entry<Screen.AppList> {
                            AppListScreen(
                                onAppClick = { backStack.add(Screen.AppDetails(it)) },
                                onSearchClick = { backStack.add(Screen.Search) },
                                onUpdatesClick = { backStack.add(Screen.Updates) },
                                onRepositoriesClick = { backStack.add(Screen.Repositories) },
                            )
                        }
                        entry<Screen.Search> {
                            SearchScreen(onAppClick = { backStack.add(Screen.AppDetails(it)) })
                        }
                        entry<Screen.Updates> {
                            UpdatesScreen(onAppClick = { backStack.add(Screen.AppDetails(it)) })
                        }
                        entry<Screen.Repositories> {
                            RepositoriesScreen(
                                onRepositoryClick = { backStack.add(Screen.RepositoryDetails(it)) },
                                onAddClick = { backStack.add(Screen.AddRepository) },
                            )
                        }
                        entry<Screen.RepositoryDetails> {
                            RepositoryDetailsScreen(it.repoId, onRemoved = { backStack.removeLastOrNull() })
                        }
                        entry<Screen.AddRepository> {
                            AddRepositoryScreen(onAdded = { backStack.removeLastOrNull() })
                        }
                        entry<Screen.AppDetails> { AppDetailsScreen(it.packageName) }
                    },
            )
        }
    }
}
