plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(project(":platform"))
}

application {
    mainClass = "hearthseed.platform.desktop.MainKt"
}
