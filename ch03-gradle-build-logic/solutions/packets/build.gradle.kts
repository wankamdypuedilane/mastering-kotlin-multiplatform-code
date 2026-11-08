// ch03-gradle-build-logic/solutions/packets/build.gradle.kts
plugins {
    id("hearthseed.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":units"))
        }
    }
}
