// hearthseed/solutions/build.gradle.kts
// Solutions of the exercises of Chapters 11 and 12 (not part of the app).
plugins {
    id("hearthseed.kmp-compose") // Exercise 12.3 is a composable
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:data"))
            implementation(project(":feature:catalog"))
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
