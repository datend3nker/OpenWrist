package io.github.datend3nker.openwrist.repo

import android.net.Uri
import androidx.core.net.toUri
import org.fdroid.database.Repository

/**
 * The address of a file of this repository, such as an APK or an icon.
 * [name] is the path from the index, relative to the repository address.
 */
fun Repository.fileUri(name: String): Uri =
    address
        .toUri()
        .buildUpon()
        .apply {
            name
                .split("/")
                .filter { it.isNotEmpty() }
                .forEach { appendPath(it) }
        }.build()
