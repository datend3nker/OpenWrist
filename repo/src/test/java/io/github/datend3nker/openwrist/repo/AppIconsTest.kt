package io.github.datend3nker.openwrist.repo

import android.net.Uri
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.fdroid.database.FDroidDatabase
import org.fdroid.database.Repository
import org.fdroid.download.Downloader
import org.fdroid.download.DownloaderFactory
import org.fdroid.index.v2.FileV2
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class AppIconsTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val repo = mockk<Repository>()
    private val db = mockk<FDroidDatabase>()
    private val downloaderFactory = mockk<DownloaderFactory>()
    private val icon = FileV2(name = "/org.example/en-US/icon.png", sha256 = "abc123", size = 4)

    private lateinit var cacheDir: File
    private lateinit var appIcons: AppIcons

    @Before
    fun setUp() {
        cacheDir = tempFolder.newFolder("icons")
        every { db.getRepositoryDao().getRepository(REPO_ID) } returns repo
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
        mockkStatic("io.github.datend3nker.openwrist.repo.RepositoryFilesKt")
        every { repo.fileUri(any()) } returns mockk<Uri>()
        appIcons = AppIcons(db, downloaderFactory, cacheDir)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `returns a cached icon without downloading it`() {
        File(cacheDir, "abc123").writeText("icon")

        val file = runBlocking { appIcons.iconFile(REPO_ID, icon) }

        assertEquals(File(cacheDir, "abc123"), file)
        verify(exactly = 0) { downloaderFactory.create(any(), any(), any(), any()) }
    }

    @Test
    fun `stores a downloaded icon under its hash`() {
        val destFile = slot<File>()
        every { downloaderFactory.create(repo, any(), icon, capture(destFile)) } answers {
            mockk<Downloader> { every { download() } answers { destFile.captured.writeText("icon") } }
        }

        val file = runBlocking { appIcons.iconFile(REPO_ID, icon) }

        assertEquals(File(cacheDir, "abc123"), file)
        assertEquals("icon", file?.readText())
        assertEquals(listOf("abc123"), cacheDir.list()?.toList())
    }

    @Test
    fun `returns null and keeps no file if the download fails`() {
        // The downloader throws this, among others, when the hash does not match.
        every { downloaderFactory.create(repo, any(), icon, any()) } returns
            mockk<Downloader> { every { download() } throws IOException("Hash not matching") }

        val file = runBlocking { appIcons.iconFile(REPO_ID, icon) }

        assertNull(file)
        assertEquals(emptyList<String>(), cacheDir.list()?.toList())
    }

    private companion object {
        const val REPO_ID = 1L
    }
}
