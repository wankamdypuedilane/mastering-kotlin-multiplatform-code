plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

val modules = listOf(":language", ":seedcount", ":solutions")

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
// allTests runs the JVM, JS (Node) and Wasm (Node) tests on Linux.
tasks.register("ciLinux") {
    dependsOn(modules.map { "$it:allTests" })
}
tasks.register("ciMacos") {
    dependsOn(modules.map { "$it:iosSimulatorArm64Test" })
}
