// ch22-release/androidApp/build.gradle.kts
import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// Release secrets: environment variable first (CI), then a Gradle property
// (~/.gradle/gradle.properties on your machine). Never in this repository.
fun secret(name: String): String? = providers.environmentVariable(name)
    .orElse(providers.gradleProperty(name)).orNull

val uploadKeystore = secret("HEARTHSEED_UPLOAD_KEYSTORE")

android {
    namespace = "com.example.hearthseed"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.example.hearthseed"
        minSdk = 24
        targetSdk = 36
        versionCode = providers.gradleProperty("release.build").get().toInt()
        versionName = providers.gradleProperty("release.version").get()
    }
    signingConfigs {
        if (uploadKeystore != null) {
            create("upload") {
                storeFile = file(uploadKeystore)
                storePassword = secret("HEARTHSEED_UPLOAD_STORE_PASSWORD")
                keyAlias = secret("HEARTHSEED_UPLOAD_KEY_ALIAS")
                keyPassword = secret("HEARTHSEED_UPLOAD_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true    // R8 (see the security chapter)
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"))
            // No key configured: build an unsigned bundle instead of failing.
            signingConfig = signingConfigs.findByName("upload")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
}

// Prints where the bundle is, how big it is and whether it carries a signature.
tasks.register("inspectReleaseBundle") {
    dependsOn("bundleRelease")
    val aab = layout.buildDirectory
        .file("outputs/bundle/release/androidApp-release.aab")
    inputs.file(aab)
    doLast {
        val file = aab.get().asFile
        val signers = ZipFile(file).use { zip ->
            zip.entries().asSequence().map { it.name }.filter {
                it.startsWith("META-INF/") && it.endsWith(".SF")
            }.toList()
        }
        val kib = file.length() / 1024
        val signed = signers.ifEmpty { "none" }
        println("${file.name}: $kib KiB, signature files: $signed")
    }
}

// A throwaway upload key for trying the signed path locally. Never reuse it.
tasks.register<Exec>("createThrowawayKeystore") {
    val store = layout.buildDirectory.file("throwaway/upload.jks")
    outputs.file(store)
    onlyIf { !store.get().asFile.exists() }
    doFirst { store.get().asFile.parentFile.mkdirs() }
    commandLine(
        "keytool", "-genkeypair", "-keystore", store.get().asFile.path,
        "-alias", "upload", "-keyalg", "RSA", "-keysize", "2048",
        "-validity", "10000", "-dname", "CN=Throwaway Upload Key",
        "-storepass", "YOUR_KEYSTORE_PASSWORD",
        "-keypass", "YOUR_KEYSTORE_PASSWORD",
    )
}
