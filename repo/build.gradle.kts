plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.datend3nker.openwrist.repo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // `api`, because the app works with the library types (AppListItem,
    // Repository, ...) directly instead of copies of them.
    api(libs.fdroid.index)
    api(libs.fdroid.download)
    api(libs.fdroid.database)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.livedata.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
