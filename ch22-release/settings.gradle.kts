// ch22-release/settings.gradle.kts
pluginManagement {
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
rootProject.name = "ch22-release"
include(":core", ":shared", ":solutions", ":androidApp", ":desktopApp", ":webApp")
