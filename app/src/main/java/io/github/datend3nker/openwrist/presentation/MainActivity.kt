/**
 * Wear OS user interface of OpenWrist: screens, navigation and the
 * translations of everything the user sees.
 */
package io.github.datend3nker.openwrist.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import io.github.datend3nker.openwrist.presentation.theme.OpenWristTheme

/** Entry point of the app on the watch. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

/** The screens of the app. Each one is an entry on the back stack. */
sealed interface Screen {
    data object AppList : Screen

    data object Search : Screen

    data class AppDetails(
        val packageName: String,
    ) : Screen
}

/** Root composable. Swiping right goes back to the previous screen. */
@Composable
fun WearApp() {
    val backStack = remember { mutableStateListOf<Screen>(Screen.AppList) }
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
                            )
                        }
                        entry<Screen.Search> {
                            SearchScreen(onAppClick = { backStack.add(Screen.AppDetails(it)) })
                        }
                        entry<Screen.AppDetails> { AppDetailsScreen(it.packageName) }
                    },
            )
        }
    }
}
