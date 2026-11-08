// hearthseed/feature/catalog/build.gradle.kts
plugins {
    id("hearthseed.kmp-compose") // Compose, Material 3, ViewModel, tests
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data")) // never another :feature
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
