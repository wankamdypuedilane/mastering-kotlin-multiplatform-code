// mendfield/server/build.gradle.kts
plugins {
    id("mendfield.jvm-server")
}

dependencies {
    // The same rules as the apps: slot checks and statistics come from here.
    implementation(project(":core:domain"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.json)
    implementation(libs.serialization.json)
    implementation(libs.logback)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
}

application {
    // ./gradlew :server:run starts the server on http://127.0.0.1:8086
    mainClass = "mendfield.server.ApplicationKt"
}
