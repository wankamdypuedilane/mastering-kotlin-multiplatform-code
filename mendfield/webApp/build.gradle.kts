// mendfield/webApp/build.gradle.kts
// The browser entry point (Kotlin/Wasm): the shared App() in a page.
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
                outputFileName = "mendfield.js"
                // The server sends no CORS headers: the development server
                // forwards /api and /health to it, so the page and the API
                // share one origin (http://localhost:8080).
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    proxy = mutableListOf(
                        KotlinWebpackConfig.DevServer.Proxy(
                            context = mutableListOf("/api", "/health"),
                            target = "http://localhost:8086",
                        ),
                    )
                }
            }
        }
        binaries.executable()
    }
    sourceSets.wasmJsMain.dependencies {
        implementation(project(":shared"))
        implementation(libs.compose.foundation)
        // Region time zones ("Europe/London") for kotlinx-datetime on Wasm.
        implementation(npm("@js-joda/timezone", libs.versions.js.joda.timezone.get()))
    }
}
