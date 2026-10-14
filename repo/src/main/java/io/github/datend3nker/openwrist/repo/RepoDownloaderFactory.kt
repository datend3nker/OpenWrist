package io.github.datend3nker.openwrist.repo

import android.net.Uri
import org.fdroid.IndexFile
import org.fdroid.database.Repository
import org.fdroid.download.DownloadRequest
import org.fdroid.download.Downloader
import org.fdroid.download.DownloaderFactory
import org.fdroid.download.HttpDownloader
import org.fdroid.download.HttpDownloaderV2
import org.fdroid.download.HttpManager
import org.fdroid.download.Mirror
import org.fdroid.index.IndexFormatVersion
import java.io.File

/**
 * Creates HTTP downloaders for files of a repository: the index and APKs.
 *
 * The downloaders try the repository's mirrors and check the SHA-256 hash of
 * every file whose [IndexFile.sha256] is set. A file with a wrong hash fails
 * the download. A simplified version of the F-Droid client's factory: no
 * proxy or Tor, no local repositories.
 */
class RepoDownloaderFactory(
    private val httpManager: HttpManager,
) : DownloaderFactory() {
    override fun create(
        repo: Repository,
        uri: Uri,
        indexFile: IndexFile,
        destFile: File,
    ): Downloader = create(repo, repo.getMirrors(), uri, indexFile, destFile, null)

    override fun create(
        repo: Repository,
        mirrors: List<Mirror>,
        uri: Uri,
        indexFile: IndexFile,
        destFile: File,
        tryFirst: Mirror?,
    ): Downloader {
        val request =
            DownloadRequest(
                indexFile = indexFile,
                mirrors = mirrors,
                username = repo.username,
                password = repo.password,
                tryFirstMirror = tryFirst,
            )
        // A repository that was never downloaded has no format version yet.
        // The index v1 downloader handles both, the v2 one only the new format.
        val v1OrUnknown = repo.formatVersion == null || repo.formatVersion == IndexFormatVersion.ONE
        return if (v1OrUnknown) {
            @Suppress("DEPRECATION")
            HttpDownloader(httpManager, request, destFile)
        } else {
            HttpDownloaderV2(httpManager, request, destFile)
        }
    }
}
