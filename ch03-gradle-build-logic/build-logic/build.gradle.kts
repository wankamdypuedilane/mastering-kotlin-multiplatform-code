// ch03-gradle-build-logic/build-logic/build.gradle.kts
plugins {
    `kotlin-dsl`
}

dependencies {
    // Plugins applied by the convention plugins must be on this classpath.
    implementation(libs.kotlin.gradle.plugin)
}
