// ch03-gradle-build-logic/build-logic/.../hearthseed.kmp-library.gradle.kts
import hearthseed.buildlogic.TargetReport
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    // No version here: it comes from build-logic's own dependencies.
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    jvm()
    js { nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

// Computed lazily, after the module's own build script has run.
val targetNames = provider {
    kotlin.targets.map { it.name }.filter { it != "metadata" }.sorted()
}

tasks.register<TargetReport>("targetReport") {
    group = "help"
    description = "Writes the Kotlin targets of this module to a file."
    modulePath.set(project.path)
    targets.set(targetNames)
    reportFile.set(layout.buildDirectory.file("reports/targets.txt"))
}
