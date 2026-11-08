// mendfield/core/data/build.gradle.kts
// Offline-first repositories: the device copy is the single source of truth.
plugins {
    id("mendfield.kmp-library") // every target, tested on every target
    id("org.jetbrains.kotlin.plugin.serialization") // KeyValueOutboxStore
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))  // models and rules
            api(project(":core:network")) // ApiResult in CafeRemote
            api(project(":core:sync"))    // outbox, SyncEngine, SyncState
            api(libs.coroutines.core)
            implementation(libs.serialization.json)
        }
        webMain.dependencies {
            implementation(libs.kotlinx.browser) // window.localStorage
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.coroutines.test)
        }
    }
}
