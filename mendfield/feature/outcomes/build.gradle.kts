// mendfield/feature/outcomes/build.gradle.kts
plugins {
    id("mendfield.kmp-compose") // Compose, Material 3, ViewModel, tests
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data")) // never another :feature
            implementation(project(":core:ui"))
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
