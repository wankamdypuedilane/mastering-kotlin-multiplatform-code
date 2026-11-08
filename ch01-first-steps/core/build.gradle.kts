plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}
kotlin {
    android {
        namespace = "demo.core"
        compileSdk = 37
        minSdk = 24
    }
    jvm()
    js { nodejs() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()
    // Exercise 1.3: a new Kotlin/Native target that runs its tests on Linux.
    linuxX64()
    sourceSets {
        commonMain.dependencies { implementation(libs.coroutines.core) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
