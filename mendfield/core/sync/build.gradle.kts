// mendfield/core/sync/build.gradle.kts
plugins {
    id("mendfield.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:network")) // transport, codec
            api(libs.coroutines.core) // Flow and StateFlow in the API
        }
        commonTest.dependencies {
            implementation(libs.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}
