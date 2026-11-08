// mendfield/settings.gradle.kts
pluginManagement {
    includeBuild("build-logic") // the mendfield.* convention plugins
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "mendfield"

// Shared, Compose-free code: every target, tested on every target.
include(":core:model", ":core:domain", ":core:network", ":core:sync")
// The local API server (JVM only); it reuses :core:model and :core:domain.
include(":server")
// Added in the second part of the capstone: ":core:database" (Room),
// ":feature:*" (Compose UI), ":shared", ":androidApp", ":desktopApp",
// ":webApp", and the SwiftUI shell in iosApp/.
