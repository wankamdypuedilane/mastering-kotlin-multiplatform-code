// hearthseed/core/network/build.gradle.kts
plugins {
    id("hearthseed.kmp-library")
    id("org.jetbrains.kotlin.plugin.serialization") // @Serializable DTOs
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.client.core)
            api(libs.ktor.client.engine.defaults) // OkHttp, Darwin, Js
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)
            api(libs.ktor.client.logging)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.mock)
            implementation(libs.coroutines.test)
        }
    }
}
