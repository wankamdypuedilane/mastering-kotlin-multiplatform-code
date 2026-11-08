// hearthseed/core/testing/build.gradle.kts
// Test doubles for the tests of the features and solutions (no app uses it).
plugins {
    id("hearthseed.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))
            implementation(libs.coroutines.core)
        }
    }
}
