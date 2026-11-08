// mendfield/core/testing/build.gradle.kts
// Test doubles and shared test suites; used only by test source sets
// (checkModuleRules refuses a main dependency on this module).
plugins {
    id("mendfield.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))
            api(kotlin("test"))           // the contracts assert
            api(libs.coroutines.test)
        }
    }
}
