// mendfield/core/ui/build.gradle.kts
// The design system the features share: theme, labels, common widgets.
plugins {
    id("mendfield.kmp-compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:data")) // DataError.userMessage()
        }
    }
}
