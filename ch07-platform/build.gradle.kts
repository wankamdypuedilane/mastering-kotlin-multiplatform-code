plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.application) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(
        ":platform:allTests", ":solutions:allTests",
        ":androidApp:assembleDebug", ":desktopApp:run",
    )
}
tasks.register("ciMacos") {
    dependsOn(":platform:iosSimulatorArm64Test", ":solutions:iosSimulatorArm64Test")
}
