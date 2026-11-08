// mendfield/desktopApp/build.gradle.kts
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
    mainClass = "mendfield.desktop.MainKt"
    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "Mendfield"
        packageVersion = "1.0.0"
        // Before packaging, run suggestRuntimeModules and list its answer
        // in modules(...): jlink keeps only the JDK modules named there.
    }
}
