// mendfield/shared/build.gradle.kts
// The app shell: navigation, the sync bar, Koin wiring, the iOS framework.
plugins {
    id("mendfield.kmp-compose")
    id("org.jetbrains.kotlin.plugin.serialization") // @Serializable routes
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared" // import Shared in Swift
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(project(":core:network"))
            implementation(project(":core:ui"))
            implementation(project(":feature:events"))
            implementation(project(":feature:intake"))
            implementation(project(":feature:outcomes"))
            implementation(libs.navigation.compose)
            api(libs.koin.core) // initKoin() returns a KoinApplication
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        // Room where it has a bundled SQLite driver; the web stores elsewhere.
        androidMain.dependencies { implementation(project(":core:database")) }
        jvmMain.dependencies { implementation(project(":core:database")) }
        iosMain.dependencies { implementation(project(":core:database")) }
        wasmJsMain.dependencies { implementation(libs.kotlinx.browser) }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
