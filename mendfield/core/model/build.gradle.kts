// mendfield/core/model/build.gradle.kts
plugins {
    id("mendfield.kmp-library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime) // LocalDate, LocalTime, TimeZone
        }
    }
}
