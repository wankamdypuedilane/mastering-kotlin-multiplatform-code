// hearthseed/webApp/build.gradle.kts
// The browser entry point (Kotlin/Wasm): the shared App() in a web shell.
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    // On the classpath through build-logic: ids without versions.
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "hearthseed.js"
                // hearthseed-server sends no CORS headers: the development
                // server forwards /api/... to it, so the page and the API
                // share one origin (http://localhost:8080).
                val server = devServer ?: KotlinWebpackConfig.DevServer()
                devServer = server.apply {
                    proxy = mutableListOf(
                        KotlinWebpackConfig.DevServer.Proxy(
                            context = mutableListOf("/api"),
                            target = "http://localhost:8085",
                        ),
                    )
                }
            }
        }
        binaries.executable()
    }
    sourceSets.wasmJsMain.dependencies {
        implementation(project(":shared"))
        implementation(project(":webApp:support"))
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.navigation.compose)
        implementation(libs.koin.compose)
        implementation(libs.kotlinx.browser)
    }
}
