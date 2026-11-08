// mendfield/build-logic/src/main/kotlin/mendfield.android-application.gradle.kts
plugins {
    id("com.android.application") // AGP 9 compiles Kotlin itself
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 37
    defaultConfig {
        minSdk = 24
        targetSdk = 36
    }
}
