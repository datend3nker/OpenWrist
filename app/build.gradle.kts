plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.datend3nker.openwrist"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // Never change this after the first release: updates are matched by
        // application ID and signing key. See ADR 0006.
        applicationId = "io.github.datend3nker.openwrist"
        minSdk = 30
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    // The release key and its passwords come from environment variables, so
    // that nothing secret ends up in the repository. Without them the
    // release APK is built unsigned. See docs/development.md.
    val releaseKeystore = providers.environmentVariable("OPENWRIST_KEYSTORE").orNull
    if (releaseKeystore != null) {
        signingConfigs {
            create("release") {
                storeFile = file(releaseKeystore)
                storePassword = providers.environmentVariable("OPENWRIST_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("OPENWRIST_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("OPENWRIST_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // The dependency metadata block is encrypted with a Google key that only
    // Google Play can read. F-Droid and IzzyOnDroid reject APKs that carry it.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":repo"))
    implementation(project(":install"))
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.wear.compose.navigation3)
    implementation(libs.wear.input)
    implementation(platform(libs.compose.bom))
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material3)
    implementation(libs.wear.compose.ui.tooling)
    implementation(libs.wear.tooling.preview)
    debugImplementation(libs.ui.tooling)
}
