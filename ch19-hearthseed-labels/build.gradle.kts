plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(
        ":core:allTests",                  // pure label logic (JVM)
        ":desktopApp:test",                // export and UI tests, headless
        ":desktopApp:createDistributable", // jpackage app image for Linux
    )
}
tasks.register("ciMacos") {
    // Desktop-only project: no iOS target. macOS runs the same tests.
    dependsOn(":core:allTests", ":desktopApp:test")
}
