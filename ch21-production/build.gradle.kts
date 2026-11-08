// ch21-production/build.gradle.kts
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
        gradle.includedBuild("build-logic").task(":test"),
        ":secure:allTests", ":secure:testAndroidHostTest",
        ":network:allTests", ":solutions:allTests",
        ":ui:compileKotlinJvm", // also writes the Compose compiler reports
        ":androidApp:assembleDebug", ":androidApp:assembleRelease", // R8
    )
}
tasks.register("ciMacos") {
    dependsOn(
        ":secure:iosSimulatorArm64Test", ":network:iosSimulatorArm64Test",
        ":solutions:iosSimulatorArm64Test", ":network:compileKotlinIosArm64",
    )
}
