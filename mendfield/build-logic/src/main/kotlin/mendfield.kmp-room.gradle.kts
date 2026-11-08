// mendfield/build-logic/src/main/kotlin/mendfield.kmp-room.gradle.kts
// A Room 3 database module for the targets with a bundled SQLite driver:
// Android, desktop JVM and iOS. The web keeps its data elsewhere (see
// :core:data, KeyValueOutboxStore): Room's web driver needs a worker script.
import mendfield.buildlogic.androidNamespace

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("com.google.devtools.ksp") // runs Room's code generator
    id("androidx.room3")          // schema export and migration inputs
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun lib(alias: String) = libs.findLibrary(alias).get()

kotlin {
    android {
        namespace = androidNamespace(project.path) // mendfield.core.database
        compileSdk = 37
        minSdk = 24
        // No host tests: the Android build of the bundled driver has native
        // code for devices only. commonTest runs on the JVM and iOS.
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    // Room generates the `actual object` of the database constructor.
    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

    sourceSets {
        commonMain.dependencies {
            api(lib("room3-runtime")) // RoomDatabase is in the public API
            implementation(lib("sqlite-bundled")) // every target has it
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(lib("coroutines-test"))
        }
    }
}

// One KSP configuration per target: Room generates code for each of them.
dependencies {
    listOf("kspAndroid", "kspJvm", "kspIosArm64", "kspIosSimulatorArm64")
        .forEach { add(it, lib("room3-compiler")) }
}

room3 {
    schemaDirectory("$projectDir/schemas") // commit these JSON files
}
