// ch21-production/settings.gradle.kts
pluginManagement {
    includeBuild("build-logic") // the app-config generator plugin
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
rootProject.name = "ch21-production"
include(":secure", ":network", ":ui", ":androidApp", ":solutions")
