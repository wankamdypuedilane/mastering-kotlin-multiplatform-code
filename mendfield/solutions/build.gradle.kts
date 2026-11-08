// mendfield/solutions/build.gradle.kts
// The solutions of the exercises of Chapter 23: small extensions of the
// data layer, tested on every target like the core modules.
plugins {
    id("mendfield.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.coroutines.test)
        }
    }
}
