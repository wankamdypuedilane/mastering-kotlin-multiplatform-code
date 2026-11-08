plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}
kotlin {
    android {
        namespace = "demo.shared"
        compileSdk = 37
        minSdk = 24
    }
    jvm()
    js { browser() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(project(":core"))
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.coroutines.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
