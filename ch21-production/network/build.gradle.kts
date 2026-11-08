// ch21-production/network/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    android {
        namespace = "hearthseed.network"
        compileSdk = 37
        minSdk = 24
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies { api(libs.ktor.client.core) }
        androidMain.dependencies { implementation(libs.ktor.client.okhttp) }
        jvmMain.dependencies { implementation(libs.ktor.client.okhttp) }
        iosMain.dependencies { implementation(libs.ktor.client.darwin) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
