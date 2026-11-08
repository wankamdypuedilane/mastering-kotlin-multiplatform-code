// hearthseed/desktopApp/build.gradle.kts
plugins {
    // On the classpath through build-logic: ids without versions.
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    jvm()
    sourceSets.jvmMain.dependencies {
        implementation(project(":shared"))
        implementation(compose.desktop.currentOs)
        implementation(libs.coroutines.swing) // Dispatchers.Main on desktop
    }
}

compose.desktop.application {
    mainClass = "hearthseed.desktop.MainKt"
}
