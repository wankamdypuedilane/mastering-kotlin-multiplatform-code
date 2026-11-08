plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(project(":client"))
    // OkHttp logs through SLF4J; this binding silences its "no provider" warning.
    runtimeOnly(libs.slf4j.nop)
}

application {
    // ./gradlew :cli:run --args="--log" with hearthseed-server running
    mainClass = "hearthseed.cli.MainKt"
}
