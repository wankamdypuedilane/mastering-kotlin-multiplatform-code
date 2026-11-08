// hearthseed/core/data/build.gradle.kts
plugins {
    id("hearthseed.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))      // repositories return domain models
            implementation(project(":core:network"))
            api(libs.coroutines.core)        // Flow in the public API
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.coroutines.test)
        }
    }
}
