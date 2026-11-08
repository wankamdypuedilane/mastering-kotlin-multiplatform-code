// hearthseed/core/database/build.gradle.kts
// Room 3 behind VarietyLocalSource: the device copy of the catalog.
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("hearthseed.kmp-library") // android, jvm, two iOS targets, wasmJs
    id("com.google.devtools.ksp") // runs Room's code generator
    id("androidx.room3")          // schema export and migration inputs
}

kotlin {
    // Room generates an `actual object` for HearthseedDatabaseConstructor.
    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        common {
            group("bundled") { // tests with BundledSQLiteDriver
                withJvm()
                group("ios") // iosArm64 and iosSimulatorArm64
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))  // VarietyLocalSource, domain models
            api(libs.room3.runtime)
            implementation(libs.sqlite.async) // suspend SQL for migrations
        }
        androidMain.dependencies { implementation(libs.sqlite.bundled) }
        jvmMain.dependencies { implementation(libs.sqlite.bundled) }
        iosMain.dependencies { implementation(libs.sqlite.bundled) }
        getByName("bundledTest").dependencies {
            implementation(libs.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.room3.testing) // MigrationTestHelper
        }
    }
}

// One KSP configuration per target: Room generates code for each of them.
dependencies {
    listOf("kspAndroid", "kspJvm", "kspIosArm64", "kspIosSimulatorArm64",
        "kspWasmJs").forEach { add(it, libs.room3.compiler) }
}

room3 {
    schemaDirectory("$projectDir/schemas") // commit these JSON files
}
