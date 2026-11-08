plugins {
    alias(libs.plugins.kotlin.multiplatform)
}
kotlin {
    jvm()
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
