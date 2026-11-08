// mendfield/core/domain/build.gradle.kts
plugins {
    id("mendfield.kmp-library") // pure logic: no I/O, no coroutines
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
        }
    }
}
