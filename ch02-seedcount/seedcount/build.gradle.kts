plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    js { nodejs() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
