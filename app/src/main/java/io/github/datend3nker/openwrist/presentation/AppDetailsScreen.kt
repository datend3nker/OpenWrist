package io.github.datend3nker.openwrist.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import io.github.datend3nker.openwrist.R
import io.github.datend3nker.openwrist.repo.AppDetails

@Composable
fun AppDetailsScreen(packageName: String) {
    val viewModel = containerViewModel(key = packageName) { AppDetailsViewModel(it.watchApps, packageName) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberTransformingLazyColumnState()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        TransformingLazyColumn(state = listState, contentPadding = contentPadding) {
            when (val current = state) {
                DetailsState.Loading -> item { CircularProgressIndicator() }
                DetailsState.NotFound ->
                    item { CenteredText(stringResource(R.string.details_not_found)) }
                is DetailsState.Loaded -> {
                    val details = current.details
                    item { ListHeader { Text(details.name, textAlign = TextAlign.Center) } }
                    details.summary?.let { item { CenteredText(it) } }
                    item { VersionText(details) }
                    details.authorName?.let { item { CenteredText(stringResource(R.string.details_author, it)) } }
                    details.license?.let { item { CenteredText(stringResource(R.string.details_license, it)) } }
                    details.description?.let { html ->
                        item {
                            Text(
                                HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT).toString().trim(),
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    item { CenteredText(details.packageName, small = true) }
                }
            }
        }
    }
}

@Composable
private fun VersionText(details: AppDetails) {
    val installed = details.installedVersionName
    val suggested = details.suggestedVersion?.versionName
    val text =
        when {
            installed != null && suggested != null ->
                stringResource(R.string.details_update_available, installed, suggested)
            installed != null -> stringResource(R.string.details_installed, installed)
            suggested != null -> stringResource(R.string.details_version, suggested)
            else -> stringResource(R.string.details_no_compatible_version)
        }
    CenteredText(text)
}

@Composable
private fun CenteredText(
    text: String,
    small: Boolean = false,
) {
    Text(
        text,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        style = if (small) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
    )
}
