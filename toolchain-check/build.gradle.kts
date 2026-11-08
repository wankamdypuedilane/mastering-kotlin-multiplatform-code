plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.application) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml for the convention).
tasks.register("ciLinux") {
    dependsOn(":core:allTests", ":shared:compileKotlinJs", ":shared:compileKotlinWasmJs",
        ":androidApp:assembleDebug", ":desktopApp:compileKotlinJvm")
}
tasks.register("ciMacos") {
    dependsOn(":core:iosSimulatorArm64Test", ":shared:linkDebugFrameworkIosSimulatorArm64")
}
