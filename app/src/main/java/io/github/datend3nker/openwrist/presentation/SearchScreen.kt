package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.datend3nker.openwrist.R

@Composable
fun SearchScreen(onAppClick: (packageName: String) -> Unit) {
    val viewModel = containerViewModel { SearchViewModel(it.watchApps) }
    val results by viewModel.results.collectAsStateWithLifecycle(initialValue = null)
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            item { ListHeader { Text(stringResource(R.string.search)) } }
            item { SearchField(onQueryChange = viewModel::search) }
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

/** A text field that searches while the user types. */
@Composable
private fun SearchField(onQueryChange: (String) -> Unit) {
    val textState = rememberTextFieldState()
    LaunchedEffect(textState) {
        snapshotFlow { textState.text.toString() }.collect(onQueryChange)
    }
    TextInput(
        state = textState,
        hint = stringResource(R.string.search_hint),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}
