// hearthseed/build-logic/src/main/kotlin/hearthseed.kmp-library.gradle.kts
// A Compose-free multiplatform library: every target of the app.
import hearthseed.buildlogic.androidNamespace
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        namespace = androidNamespace(project.path) // hearthseed.core.data
        compileSdk = 37
        minSdk = 24
    }
    jvm()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() } // no Compose here, so tests can run on Node.js
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
