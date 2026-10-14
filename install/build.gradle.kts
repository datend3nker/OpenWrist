plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.datend3nker.openwrist.install"
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
    // `api`, because callers pass the library types (Repository, AppVersion) in.
    api(project(":repo"))
    implementation(libs.core.ktx)

    testImplementation(libs.junit)
}
