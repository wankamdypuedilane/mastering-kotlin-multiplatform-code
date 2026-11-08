// ch22-release/desktopApp/build.gradle.kts
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
}

kotlin {
    jvm()
    sourceSets.jvmMain.dependencies {
        implementation(project(":shared"))
        implementation(compose.desktop.currentOs)
        implementation(libs.coroutines.swing)
    }
}

fun secret(name: String): String? = providers.environmentVariable(name).orNull

compose.desktop.application {
    mainClass = "release.MainKt"
    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "hearthseed"
        packageVersion = providers.gradleProperty("release.version").get()
        vendor = "Larkmoor Commons Seed Library"
        macOS {
            bundleID = "dev.example.hearthseed.desktop"
            val signingIdentity = secret("MACOS_SIGNING_IDENTITY")
            signing {
                sign.set(signingIdentity != null)
                signingIdentity?.let { identity.set(it) }
            }
            notarization {
                secret("MACOS_NOTARY_APPLE_ID")?.let { appleID.set(it) }
                secret("MACOS_NOTARY_PASSWORD")?.let { password.set(it) }
                secret("MACOS_NOTARY_TEAM_ID")?.let { teamID.set(it) }
            }
        }
        windows {
            // Generated once, never changed: MSI upgrades find the app by it.
            upgradeUuid = "3f6c2a8e-5b1d-4e7a-9c40-2d8b7e1f0a65"
        }
        linux { debMaintainer = "releases@example.org" }
    }
}
