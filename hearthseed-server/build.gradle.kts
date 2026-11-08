plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
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
    // ./gradlew run starts the server on http://127.0.0.1:8085
    mainClass = "hearthseed.server.ApplicationKt"
}

tasks.test {
    useJUnitPlatform()
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") { dependsOn("test") }
tasks.register("ciMacos") { dependsOn("test") }
