// ch21-production/build-logic/settings.gradle.kts
pluginManagement {
    repositories { gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}
rootProject.name = "build-logic"
