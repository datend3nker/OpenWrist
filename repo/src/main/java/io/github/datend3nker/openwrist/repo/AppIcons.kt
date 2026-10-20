package io.github.datend3nker.openwrist.repo

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.fdroid.database.FDroidDatabase
import org.fdroid.download.DownloaderFactory
import org.fdroid.index.v2.FileV2
import java.io.File

/**
 * Downloads app icons from their repository and keeps them in [cacheDir].
 *
 * Icons are listed in the signed index with their SHA-256 hash, and the
 * downloader rejects a file with a different hash, like for APKs. A cached
 * icon is named after its hash, so a changed icon is downloaded again.
 */
class AppIcons(
    private val db: FDroidDatabase,
    private val downloaderFactory: DownloaderFactory,
    private val cacheDir: File,
) {
    /**
     * Returns [icon] of an app from the repository with [repoId] as a local
     * file, downloading it first if needed. Returns null if the download
     * fails, for example without network; the caller then shows no icon.
     */
    suspend fun iconFile(
        repoId: Long,
        icon: FileV2,
    ): File? =
        withContext(Dispatchers.IO) {
            val file = File(cacheDir, cacheName(icon))
            if (file.isFile) return@withContext file
            @Suppress("TooGenericExceptionCaught") // the library throws IOException, Ktor and its own exceptions
            try {
                download(repoId, icon, file)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Downloading icon ${icon.name} failed", e)
                null
            }
        }

    private fun download(
        repoId: Long,
        icon: FileV2,
        file: File,
    ): File? {
        val repo = db.getRepositoryDao().getRepository(repoId) ?: return null
        cacheDir.mkdirs()
        // Download into a file of its own and rename it when complete, so a
        // second request for the same icon never sees half a file.
        val partFile = File.createTempFile(file.name, ".part", cacheDir)
        try {
            downloaderFactory.create(repo, repo.fileUri(icon.name), icon, partFile).download()
            return if (partFile.renameTo(file)) file else null
        } finally {
            partFile.delete()
        }
    }

    /** The hash if the index has one, otherwise the path in the repository. */
    private fun cacheName(icon: FileV2): String = icon.sha256 ?: icon.name.replace('/', '_')

    private companion object {
        const val TAG = "AppIcons"
    }
}
