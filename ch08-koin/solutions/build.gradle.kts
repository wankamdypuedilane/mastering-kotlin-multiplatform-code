plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}
kotlin {
    android {
        namespace = "hearthseed.labels.solutions"
        compileSdk = 37
        minSdk = 24
    }
    jvm()
    js { nodejs() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.lifecycle.viewmodel)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.koin.test)
        }
    }
}
