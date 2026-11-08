plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.application) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(
        ":core:allTests",       // domain + fake repository, every target
        ":shared:jvmTest",      // ViewModel tests + desktop UI test
        ":shared:compileKotlinJs", ":shared:compileKotlinWasmJs",
        ":androidApp:assembleDebug", ":desktopApp:compileKotlinJvm",
    )
}
tasks.register("ciMacos") {
    dependsOn(
        ":core:iosSimulatorArm64Test", ":shared:iosSimulatorArm64Test",
        ":shared:linkDebugFrameworkIosSimulatorArm64",
    )
}
