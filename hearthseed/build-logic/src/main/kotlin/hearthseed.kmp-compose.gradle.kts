// hearthseed/build-logic/src/main/kotlin/hearthseed.kmp-compose.gradle.kts
// A multiplatform library with Compose UI and ViewModels (features, shared).
import hearthseed.buildlogic.androidNamespace
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

// A precompiled script plugin reads the catalog through the extension.
val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun lib(alias: String) = libs.findLibrary(alias).get()

kotlin {
    android {
        namespace = androidNamespace(project.path)
        compileSdk = 37
        minSdk = 24
    }
    jvm()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() } // Compose for web runs in a browser (Chapter 18)
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(lib("compose-runtime"))
            implementation(lib("compose-foundation"))
            implementation(lib("compose-material3"))
            implementation(lib("lifecycle-viewmodel-compose"))
            implementation(lib("lifecycle-runtime-compose"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(lib("coroutines-test"))
        }
        jvmTest.dependencies {
            // Desktop UI tests (runComposeUiTest) need Skiko for this OS.
            implementation(lib("compose-ui-test"))
            implementation(compose.desktop.currentOs)
        }
    }
}
