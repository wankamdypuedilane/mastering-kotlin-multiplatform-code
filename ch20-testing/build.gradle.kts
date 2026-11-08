plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.kover) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(
        ":core:allTests",       // JVM, Android host, JS and Wasm (Node.js)
        ":core:koverVerify",    // line coverage must stay above the bound
        ":ui:jvmTest",          // Compose UI tests on the desktop JVM
        ":ui:compileAndroidMain",
    )
}
tasks.register("ciMacos") {
    dependsOn(":core:iosSimulatorArm64Test", ":ui:iosSimulatorArm64Test")
}
