plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}
android {
    namespace = "hearthseed.labels.android"
    compileSdk = 37
    defaultConfig {
        applicationId = "hearthseed.labels.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
    implementation(libs.koin.android)
}
