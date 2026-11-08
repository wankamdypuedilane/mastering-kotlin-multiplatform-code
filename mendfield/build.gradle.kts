// mendfield/build.gradle.kts
plugins {
    // Load the convention plugins (and AGP, KGP) once, in the root class
    // loader, so that every module shares the same plugin classes.
    id("mendfield.kmp-library") apply false
    id("mendfield.jvm-server") apply false
    id("mendfield.module-rules") // checkModuleRules
}

// Compose-free modules: allTests runs the JVM, JS and Wasm (Node.js) tests.
val libraryModules =
    listOf(":core:model", ":core:domain", ":core:network", ":core:sync")

// Aggregate tasks used by CI (see .github/workflows/ci.yml).
tasks.register("ciLinux") {
    dependsOn(libraryModules.map { "$it:allTests" })
    dependsOn(libraryModules.map { "$it:compileAndroidMain" })
    dependsOn(":server:test")
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
    dependsOn("checkModuleRules")
}
tasks.register("ciMacos") {
    dependsOn(libraryModules.map { "$it:iosSimulatorArm64Test" })
}
