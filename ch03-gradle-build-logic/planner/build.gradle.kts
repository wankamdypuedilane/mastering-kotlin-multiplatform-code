// ch03-gradle-build-logic/planner/build.gradle.kts
plugins {
    id("hearthseed.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // api: Grams appears in the public API of :planner.
            api(project(":units"))
        }
    }
}
