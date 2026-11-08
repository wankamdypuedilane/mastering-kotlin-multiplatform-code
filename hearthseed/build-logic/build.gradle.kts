// hearthseed/build-logic/build.gradle.kts
plugins {
    `kotlin-dsl`
}

dependencies {
    // Every plugin a convention plugin applies must be on this classpath.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.serialization.gradle.plugin)
    implementation(libs.compose.gradle.plugin)
    implementation(libs.android.gradle.plugin)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
