plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}
kotlin {
    jvm()
    sourceSets.jvmMain.dependencies {
        implementation(project(":shared"))
        implementation(compose.desktop.currentOs)
        implementation(libs.coroutines.swing)
        implementation(libs.koin.compose)
    }
}
compose.desktop.application { mainClass = "hearthseed.labels.desktop.MainKt" }
