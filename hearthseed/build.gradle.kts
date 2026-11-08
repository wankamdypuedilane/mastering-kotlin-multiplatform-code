// hearthseed/build.gradle.kts
plugins {
    // Load the convention plugins (and AGP, KGP, Compose) once, in the root
    // class loader, so that every module shares the same plugin classes.
    id("hearthseed.kmp-library") apply false
    id("hearthseed.kmp-compose") apply false
    id("hearthseed.android-application") apply false
    id("hearthseed.module-rules") // checkModuleRules (Exercise 11.4)
}

// Compose-free modules: allTests runs JVM and Wasm (Node.js) tests.
val libraryModules =
    listOf(":core:model", ":core:network", ":core:data", ":core:database")
// Compose modules: JVM tests only (Wasm tests would need Skiko in a browser).
val composeModules =
    listOf(":feature:catalog", ":feature:loans", ":shared", ":solutions")

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(libraryModules.map { "$it:allTests" })
    dependsOn(composeModules.map { "$it:jvmTest" })
    dependsOn(":shared:compileKotlinWasmJs", ":androidApp:assembleDebug",
        ":desktopApp:compileKotlinJvm")
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
    dependsOn("checkModuleRules")
}
tasks.register("ciMacos") {
    val modules = libraryModules + composeModules
    dependsOn(modules.map { "$it:iosSimulatorArm64Test" })
    dependsOn(":shared:linkDebugFrameworkIosSimulatorArm64")
}
