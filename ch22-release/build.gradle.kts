// ch22-release/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.maven.publish) apply false
}

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(
        ":core:allTests", ":solutions:allTests",
        ":androidApp:bundleRelease", ":androidApp:inspectReleaseBundle",
        ":desktopApp:createDistributable",
        ":webApp:wasmJsBrowserDistribution",
        ":core:publishToMavenLocal",
    )
}
tasks.register("ciMacos") {
    dependsOn(
        ":core:iosSimulatorArm64Test", ":solutions:iosSimulatorArm64Test",
        ":shared:linkReleaseFrameworkIosArm64",
    )
}
