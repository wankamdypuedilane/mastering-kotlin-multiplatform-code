plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kover)
}
kotlin {
    android {
        namespace = "seedloans.core"
        compileSdk = 37
        minSdk = 24
        withHostTest {}                 // runs commonTest on the local JVM
    }
    jvm()
    js { nodejs() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.datetime)
            api(libs.coroutines.core)
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.kermit.test)
        }
    }
}
kover {
    reports {
        verify {
            rule {
                minBound(80)            // percent of lines covered by JVM tests
            }
        }
    }
}
