package io.github.datend3nker.openwrist.repo

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.fdroid.database.FDroidDatabase
import org.fdroid.database.InitialRepository
import org.fdroid.database.RepositoryDao
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.MessageDigest

class DefaultRepositoriesTest {
    private val inserted = mutableListOf<InitialRepository>()
    private val repositoryDao =
        mockk<RepositoryDao> {
            every { insert(any<InitialRepository>()) } answers {
                inserted += firstArg<InitialRepository>()
                inserted.size.toLong()
            }
        }
    private val db =
        mockk<FDroidDatabase> {
            every { getRepositoryDao() } returns repositoryDao
            every { runInTransaction(any<Runnable>()) } answers { firstArg<Runnable>().run() }
        }

    @Test
    fun `adds f-droid org enabled and its archive disabled`() {
        DefaultRepositories().prePopulateDb(db)

        verify(exactly = 2) { repositoryDao.insert(any<InitialRepository>()) }
        assertEquals(listOf("https://f-droid.org/repo", "https://f-droid.org/archive"), inserted.map { it.address })
        assertEquals(listOf(true, false), inserted.map { it.enabled })
    }

    @Test
    fun `uses the certificate f-droid org publishes`() {
        DefaultRepositories().prePopulateDb(db)

        inserted.forEach { repo ->
            assertEquals(FDROID_FINGERPRINT, sha256(repo.certificate))
        }
    }

    private fun sha256(hexCertificate: String): String {
        val bytes = hexCertificate.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02X".format(it) }
    }

    private companion object {
        /** From https://f-droid.org/docs/Release_Channels_and_Signing_Keys/ */
        const val FDROID_FINGERPRINT = "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB"
    }
}
