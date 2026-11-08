// hearthseed/androidApp/build.gradle.kts
plugins {
    id("hearthseed.android-application") // compileSdk 37, minSdk 24, targetSdk 36
}

android {
    namespace = "hearthseed.android"
    defaultConfig {
        applicationId = "dev.example.hearthseed"
        versionCode = 1
        versionName = "0.12.0"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
    implementation(libs.koin.android)
}
