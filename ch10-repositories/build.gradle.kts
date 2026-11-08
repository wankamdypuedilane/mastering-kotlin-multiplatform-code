plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
// Both modules are Compose-free, so allTests runs JVM, JS and Wasm (Node) tests.
tasks.register("ciLinux") {
    dependsOn(":data:allTests", ":solutions:allTests")
}
tasks.register("ciMacos") {
    dependsOn(":data:iosSimulatorArm64Test", ":solutions:iosSimulatorArm64Test")
}
