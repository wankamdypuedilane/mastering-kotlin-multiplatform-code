// ch22-release/webApp/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

kotlin {
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser { commonWebpackConfig { outputFileName = "hearthseed.js" } }
        binaries.executable()
    }
    sourceSets.wasmJsMain.dependencies {
        implementation(project(":shared"))
        implementation(libs.compose.ui)
    }
}
