package io.github.datend3nker.openwrist.presentation

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import io.github.datend3nker.openwrist.OpenWristApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fdroid.index.v2.FileV2
import java.io.File

/**
 * The icon of an app, loaded from its repository. Takes the same space while
 * loading and when there is no icon, so the layout does not jump.
 *
 * The icon is decorative: the app name is always shown next to it.
 */
@Composable
fun AppIcon(
    repoId: Long,
    icon: FileV2?,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val appIcons = (LocalContext.current.applicationContext as OpenWristApplication).container.appIcons
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val bitmap by produceState<ImageBitmap?>(initialValue = null, repoId, icon, sizePx) {
        val file = icon?.let { appIcons.iconFile(repoId, it) }
        value = file?.let { decodeIcon(it, sizePx) }
    }
    Box(modifier.size(size)) {
        bitmap?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize()) }
    }
}

/**
 * Decodes [file], scaled down by powers of two while it stays at least
 * [sizePx] wide and high, so a large icon does not take more memory than
 * needed. Returns null if the file is not an image.
 */
private suspend fun decodeIcon(
    file: File,
    sizePx: Int,
): ImageBitmap? =
    withContext(Dispatchers.IO) {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= sizePx && bounds.outHeight / (sampleSize * 2) >= sizePx) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeFile(file.path, options)?.asImageBitmap()
    }
