import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

dependencies {
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.material3)
    implementation(libs.coroutines.swing)
    testImplementation(kotlin("test"))
    testImplementation(libs.compose.ui.test)
}

tasks.test {
    systemProperty("java.awt.headless", "true") // no display on CI
}

compose.desktop {
    application {
        mainClass = "labels.app.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "hearthseed-labels"
            packageVersion = "1.0.0"
            description = "Seed-packet label designer"
            vendor = "Larkmoor Commons Seed Library"
            // as listed by the suggestRuntimeModules task
            modules("java.instrument", "jdk.unsupported")
            linux {
                debMaintainer = "labels@example.org"
                menuGroup = "Graphics"
            }
        }
    }
}
