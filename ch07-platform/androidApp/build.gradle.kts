plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "hearthseed.platform.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "hearthseed.platform.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(project(":platform"))
}
