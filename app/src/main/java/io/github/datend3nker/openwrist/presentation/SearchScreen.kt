package io.github.datend3nker.openwrist.presentation

import android.app.RemoteInput
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.input.RemoteInputIntentHelper
import io.github.datend3nker.openwrist.R

/** Key of the typed text in the result of the system input screen. */
private const val SEARCH_INPUT_KEY = "search"

@Composable
fun SearchScreen(onAppClick: (packageName: String) -> Unit) {
    val viewModel = containerViewModel { SearchViewModel(it.watchApps) }
    val results by viewModel.results.collectAsStateWithLifecycle(initialValue = null)
    val query by viewModel.query.collectAsStateWithLifecycle()
    val listState = rememberTransformingLazyColumnState()

    val hint = stringResource(R.string.search_hint)
    val inputLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data ?: return@rememberLauncherForActivityResult
            val text = RemoteInput.getResultsFromIntent(data)?.getCharSequence(SEARCH_INPUT_KEY)
            if (text != null) viewModel.search(text.toString())
        }
    val openInput = { inputLauncher.launch(searchInputIntent(hint)) }

    // Opening the search screen means the user wants to type, so start with the input.
    var inputOpened by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!inputOpened) {
            inputOpened = true
            openInput()
        }
    }

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.search)) } }
            item {
                Button(
                    onClick = openInput,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(query.ifEmpty { hint }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                )
            }
            val list = results
            if (list != null && list.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.search_no_results),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            items(list.orEmpty(), key = { it.packageName }) { app ->
                AppItem(app, onClick = { onAppClick(app.packageName) })
            }
        }
    }
}

/**
 * Opens the system input screen of Wear OS. It is the usual way to enter
 * text on a watch and offers voice input and emoji next to the keyboard.
 */
private fun searchInputIntent(label: String): Intent {
    val remoteInput = RemoteInput.Builder(SEARCH_INPUT_KEY).setLabel(label).build()
    return RemoteInputIntentHelper.createActionRemoteInputIntent().also {
        RemoteInputIntentHelper.putRemoteInputsExtra(it, listOf(remoteInput))
    }
}
