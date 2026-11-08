plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}
android {
    namespace = "seedswap.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.example.seedswap"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
}
