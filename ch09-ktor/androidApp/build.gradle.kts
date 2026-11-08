plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "hearthseed.network.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "hearthseed.network.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":client"))
    implementation(libs.coroutines.android) // Dispatchers.Main on Android
}
