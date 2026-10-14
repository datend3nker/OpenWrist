package io.github.datend3nker.openwrist.repo

import org.fdroid.database.FDroidDatabase
import org.fdroid.database.FDroidFixture
import org.fdroid.database.InitialRepository

/**
 * Adds the repositories OpenWrist starts with when the database is created:
 * the f-droid.org repository (enabled) and its archive (disabled).
 *
 * The certificate is the public signing certificate of f-droid.org, as in
 * the F-Droid client's default repositories. Its SHA-256 fingerprint is
 * 43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB, the one
 * f-droid.org publishes. The index is only accepted if it is signed with it.
 * Mirrors are left out; the first index download brings them.
 */
class DefaultRepositories : FDroidFixture {
    override fun prePopulateDb(db: FDroidDatabase) {
        db.runInTransaction {
            REPOSITORIES.forEach { db.getRepositoryDao().insert(it) }
        }
    }

    private companion object {
        const val FDROID_CERTIFICATE =
            "3082035e30820246a00302010202044c49cd00300d06092a864886f70d01010505003071310b3009" +
                "06035504061302554b3110300e06035504081307556e6b6e6f776e3111300f060355040713085765" +
                "7468657262793110300e060355040a1307556e6b6e6f776e3110300e060355040b1307556e6b6e6f" +
                "776e311930170603550403131043696172616e2047756c746e69656b73301e170d31303037323331" +
                "37313032345a170d3337313230383137313032345a3071310b300906035504061302554b3110300e" +
                "06035504081307556e6b6e6f776e3111300f0603550407130857657468657262793110300e060355" +
                "040a1307556e6b6e6f776e3110300e060355040b1307556e6b6e6f776e3119301706035504031310" +
                "43696172616e2047756c746e69656b7330820122300d06092a864886f70d01010105000382010f00" +
                "3082010a028201010096d075e47c014e7822c89fd67f795d23203e2a8843f53ba4e6b1bf5f2fd0e2" +
                "25938267cfcae7fbf4fe596346afbaf4070fdb91f66fbcdf2348a3d92430502824f80517b156fab0" +
                "0809bdc8e631bfa9afd42d9045ab5fd6d28d9e140afc1300917b19b7c6c4df4a494cf1f7cb4a63c8" +
                "0d734265d735af9e4f09455f427aa65a53563f87b336ca2c19d244fcbba617ba0b19e56ed34afe0b" +
                "253ab91e2fdb1271f1b9e3c3232027ed8862a112f0706e234cf236914b939bcf959821ecb2a6c180" +
                "57e070de3428046d94b175e1d89bd795e535499a091f5bc65a79d539a8d43891ec504058acb28c08" +
                "393b5718b57600a211e803f4a634e5c57f25b9b8c4422c6fd90203010001300d06092a864886f70d" +
                "0101050500038201010008e4ef699e9807677ff56753da73efb2390d5ae2c17e4db691d5df7a7b60" +
                "fc071ae509c5414be7d5da74df2811e83d3668c4a0b1abc84b9fa7d96b4cdf30bba68517ad2a93e2" +
                "33b042972ac0553a4801c9ebe07bf57ebe9a3b3d6d663965260e50f3b8f46db0531761e60340a2bd" +
                "dc3426098397fda54044a17e5244549f9869b460ca5e6e216b6f6a2db0580b480ca2afe6ec6b46ee" +
                "dacfa4aa45038809ece0c5978653d6c85f678e7f5a2156d1bedd8117751e64a4b0dcd140f3040b02" +
                "1821a8d93aed8d01ba36db6c82372211fed714d9a32607038cdfd565bd529ffc637212aaa2c224ef" +
                "22b603eccefb5bf1e085c191d4b24fe742b17ab3f55d4e6f05ef"
        val REPOSITORIES =
            listOf(
                InitialRepository(
                    name = "F-Droid",
                    address = "https://f-droid.org/repo",
                    description = "",
                    certificate = FDROID_CERTIFICATE,
                    version = 1,
                    enabled = true,
                ),
                InitialRepository(
                    name = "F-Droid Archive",
                    address = "https://f-droid.org/archive",
                    description = "",
                    certificate = FDROID_CERTIFICATE,
                    version = 1,
                    enabled = false,
                ),
            )
    }
}
