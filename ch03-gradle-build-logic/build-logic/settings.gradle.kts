// ch03-gradle-build-logic/build-logic/settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal() // where the `kotlin-dsl` plugin comes from
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    // An included build does not see the main build's catalog: import it.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
