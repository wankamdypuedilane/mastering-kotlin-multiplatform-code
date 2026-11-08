plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.application) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
// allTests runs the JVM, JS (Node) and Wasm (Node) tests on Linux.
tasks.register("ciLinux") {
    dependsOn(
        ":client:allTests", ":solutions:allTests",
        ":cli:build", ":androidApp:assembleDebug",
    )
}
tasks.register("ciMacos") {
    dependsOn(":client:iosSimulatorArm64Test", ":solutions:iosSimulatorArm64Test")
}
