// mendfield/build-logic/src/main/kotlin/mendfield.jvm-server.gradle.kts
// A Ktor server: plain Kotlin/JVM with kotlinx.serialization and `run`.
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
