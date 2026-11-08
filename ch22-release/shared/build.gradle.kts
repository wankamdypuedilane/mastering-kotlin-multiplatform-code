// ch22-release/shared/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

kotlin {
    android {
        namespace = "dev.example.hearthseed.release.shared"
        compileSdk = 37
        minSdk = 24
    }
    jvm()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(project(":core"))
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
        }
    }
}

// One version for every platform: generate a constant from gradle.properties.
val generateAppVersion = tasks.register("generateAppVersion") {
    val version = providers.gradleProperty("release.version")
    val outDir = layout.buildDirectory.dir("generated/appVersion")
    inputs.property("version", version)
    outputs.dir(outDir)
    doLast {
        val file = outDir.get().file("release/AppVersion.kt").asFile
        file.parentFile.mkdirs()
        file.writeText("package release\n\nconst val APP_VERSION: String = \"${version.get()}\"\n")
    }
}
kotlin.sourceSets.commonMain { kotlin.srcDir(generateAppVersion) }
