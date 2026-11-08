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
// Part 2: data and persistence, then the Compose UI and the apps.
include(":core:data", ":core:database", ":core:testing", ":core:ui")
include(":feature:events", ":feature:intake", ":feature:outcomes")
include(":shared", ":androidApp", ":desktopApp", ":webApp")
// The solutions of the exercises of Chapter 23.
include(":solutions")
// The SwiftUI shell is an Xcode project (XcodeGen) in iosApp/.
