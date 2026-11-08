// hearthseed/shared/build.gradle.kts
// The app shell: theme, navigation, Koin wiring, and the iOS framework.
plugins {
    id("hearthseed.kmp-compose")
    id("org.jetbrains.kotlin.plugin.serialization") // @Serializable routes
}

kotlin {
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared" // import Shared in Swift
            isStatic = true
            // Domain types and repositories, visible to Swift (Chapter 16).
            export(project(":core:model"))
            export(project(":core:data"))
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:data"))
            implementation(project(":core:network"))
            implementation(project(":core:database")) // Room (Chapter 14)
            implementation(project(":feature:catalog"))
            implementation(project(":feature:loans"))
            implementation(libs.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
