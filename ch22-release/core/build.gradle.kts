// ch22-release/core/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    android {
        namespace = "dev.example.hearthseed.release.core"
        compileSdk = 37
        minSdk = 24
    }
    jvm()
    js { nodejs() }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}

mavenPublishing {
    publishToMavenCentral()
    // Sign only when a key is configured (CI secrets); local runs do not.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }
    coordinates(
        groupId = "dev.example.hearthseed",
        artifactId = "release-core",
        version = providers.gradleProperty("release.version").get(),
    )
    pom {
        name = "Hearthseed release core"
        description = "Version rules shared by the Hearthseed apps."
        url = "https://github.com/YOUR_ACCOUNT/hearthseed"
        licenses {
            license {
                name = "Apache-2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers { developer { id = "YOUR_ACCOUNT"; name = "YOUR_NAME" } }
        scm { url = "https://github.com/YOUR_ACCOUNT/hearthseed" }
    }
}
