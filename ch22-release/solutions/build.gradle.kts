// ch22-release/solutions/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()
    js { nodejs() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies { implementation(project(":core")) }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

// Challenge 22.4: serve the production web build locally with real headers.
tasks.register<JavaExec>("serveWeb") {
    dependsOn("jvmMainClasses")
    mainClass = "release.solutions.StaticSiteServerKt"
    classpath = files(kotlin.jvm().compilations["main"].output.allOutputs,
        kotlin.jvm().compilations["main"].runtimeDependencyFiles)
    workingDir = rootDir
}
