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

    buildTypes {
        release {
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":repo"))
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
