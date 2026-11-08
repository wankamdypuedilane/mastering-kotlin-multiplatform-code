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
        commonMain.dependencies {
            implementation(libs.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }
    }
}

// ./gradlew :timer:runDemo runs the JVM demo (runBlocking, real time).
val jvmMain = kotlin.jvm().compilations.getByName("main")
tasks.register<JavaExec>("runDemo") {
    group = "application"
    classpath(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
    mainClass.set("hearthseed.germination.DemoKt")
}
