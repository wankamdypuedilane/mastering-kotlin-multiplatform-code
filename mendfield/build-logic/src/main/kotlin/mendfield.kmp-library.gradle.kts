// mendfield/build-logic/src/main/kotlin/mendfield.kmp-library.gradle.kts
// A Compose-free multiplatform library: every target of the apps, plus JS
// so that the shared logic is also tested on a second JavaScript-family VM.
import mendfield.buildlogic.androidNamespace
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    android {
        namespace = androidNamespace(project.path) // mendfield.core.domain
        compileSdk = 37
        minSdk = 24
        withHostTest {} // commonTest also runs on the local JVM as Android code
    }
    jvm()
    js { nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() } // no Compose here: Node.js can run the tests
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
