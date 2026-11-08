plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(":timer:allTests", ":solutions:allTests")
}
tasks.register("ciMacos") {
    dependsOn(":timer:iosSimulatorArm64Test", ":solutions:iosSimulatorArm64Test")
}
