// ch03-gradle-build-logic/build.gradle.kts
// The root project has no code: it only registers the CI entry points.
val libraryModules = listOf(
    ":units", ":planner",
    ":solutions:packets", ":solutions:family",
)

tasks.register("ciLinux") {
    group = "verification"
    description = "JVM, JS and Wasm tests of every library module."
    dependsOn(libraryModules.map { "$it:allTests" })
    dependsOn(libraryModules.map { "$it:targetReport" })
    dependsOn(":solutions:family:sourceStats")
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
}

tasks.register("ciMacos") {
    group = "verification"
    description = "Kotlin/Native tests on the iOS simulator."
    dependsOn(libraryModules.map { "$it:iosSimulatorArm64Test" })
}
