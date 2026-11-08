// hearthseed/settings.gradle.kts
pluginManagement {
    includeBuild("build-logic") // the hearthseed.* convention plugins
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

rootProject.name = "hearthseed"

include(":core:model", ":core:network", ":core:data", ":core:database",
    ":core:testing")
include(":feature:catalog", ":feature:loans")
include(":shared", ":androidApp", ":desktopApp")
include(":solutions") // exercises of Chapters 11 and 12
