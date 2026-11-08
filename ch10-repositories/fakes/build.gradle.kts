plugins {
    alias(libs.plugins.kotlin.multiplatform)
}
// Test doubles shared by the tests of :data and :solutions (KMP has no testFixtures).
kotlin {
    jvm()
    js { nodejs() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(project(":data"))
            implementation(libs.coroutines.core)
        }
    }
}
