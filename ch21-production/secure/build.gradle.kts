// ch21-production/secure/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.kmp.library)
    id("hearthseed.app-config") // generates hearthseed.config.AppConfig
}

appConfig {
    packageName = "hearthseed.config"
}

kotlin {
    android {
        namespace = "hearthseed.secure"
        compileSdk = 37
        minSdk = 24
        withHostTest {} // androidHostTest: AES-GCM tests on the JVM
    }
    jvm()
    js { nodejs() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.serialization.json)
            api(libs.kermit)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }
        getByName("androidHostTest").dependencies {
            implementation(kotlin("test"))
        }
    }
}
