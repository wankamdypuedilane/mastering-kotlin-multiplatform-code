// hearthseed/webApp/support/build.gradle.kts
// The web app's logic without Compose: tested on the JVM and on Node.js.
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    jvm() // runs commonTest locally in seconds
    js { nodejs() } // Kotlin/JS: webMain code plus a @JsExport (jsMain)
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() }

    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core) // StateFlow in the public API
            implementation(libs.serialization.json)
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser) // window, Storage, events
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
