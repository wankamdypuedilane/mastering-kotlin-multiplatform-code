// ch21-production/androidApp/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "hearthseed.production"
    compileSdk = 37
    defaultConfig {
        applicationId = "hearthseed.production"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = true    // R8: shrink, optimize, obfuscate code
            isShrinkResources = true  // then drop resources nothing uses
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

dependencies {
    implementation(project(":secure"))
    implementation(libs.coroutines.android)
}
