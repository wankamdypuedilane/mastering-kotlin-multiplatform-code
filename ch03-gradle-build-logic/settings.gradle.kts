// ch03-gradle-build-logic/settings.gradle.kts
pluginManagement {
    // Convention plugins come from this included build.
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "ch03-gradle-build-logic"

include(":units", ":planner")

// Exercise solutions (see the end of Chapter 3).
include(":solutions:packets", ":solutions:family")
