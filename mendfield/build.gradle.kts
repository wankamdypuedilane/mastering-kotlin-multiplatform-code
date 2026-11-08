// mendfield/build.gradle.kts
plugins {
    // Load the convention plugins (and AGP, KGP, Compose, KSP, Room) once,
    // in the root class loader, so that every module shares their classes.
    id("mendfield.kmp-library") apply false
    id("mendfield.kmp-compose") apply false
    id("mendfield.kmp-room") apply false
    id("mendfield.android-application") apply false
    id("mendfield.jvm-server") apply false
    id("mendfield.module-rules") // checkModuleRules
}

// Compose-free modules: allTests runs JVM, Android host, JS and Wasm
// (Node.js) tests. :core:testing has no tests of its own; it compiles here.
val libraryModules = listOf(":core:model", ":core:domain", ":core:network",
    ":core:sync", ":core:data", ":core:testing")
// Room: JVM tests here, iOS simulator tests on macOS (no web targets).
val databaseModules = listOf(":core:database")
// Compose modules: JVM tests only (Wasm tests would need Skiko in a browser).
val composeModules = listOf(":core:ui", ":feature:events", ":feature:intake",
    ":feature:outcomes", ":shared")

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(libraryModules.map { "$it:allTests" })
    dependsOn((libraryModules + databaseModules).map { "$it:compileAndroidMain" })
    dependsOn(databaseModules.map { "$it:jvmTest" })
    dependsOn(composeModules.map { "$it:jvmTest" })
    dependsOn(":server:test")
    dependsOn(":androidApp:assembleDebug", ":desktopApp:compileKotlinJvm")
    // The web app: compiled and bundled (the bundle downloads npm packages).
    dependsOn(":webApp:compileKotlinWasmJs", ":webApp:wasmJsBrowserDistribution")
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
    dependsOn("checkModuleRules")
}
tasks.register("ciMacos") {
    val modules = libraryModules + databaseModules + composeModules
    dependsOn(modules.map { "$it:iosSimulatorArm64Test" })
    dependsOn(":shared:linkDebugFrameworkIosSimulatorArm64")
}
