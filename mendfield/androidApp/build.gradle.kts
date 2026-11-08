// mendfield/androidApp/build.gradle.kts
plugins {
    id("mendfield.android-application") // compileSdk 37, minSdk 24, targetSdk 36
}

android {
    namespace = "mendfield.android"
    defaultConfig {
        applicationId = "dev.example.mendfield"
        versionCode = 1
        versionName = "0.23.0"
        // The emulator's alias of the host, where :server runs.
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8086/api/v1/\"")
    }
    buildFeatures {
        buildConfig = true // off by default since AGP 8.0
    }
    buildTypes {
        release {
            // Off until release preparation (README checklist): turning R8
            // on needs the Room keep rule of proguard-rules.pro and a test
            // of the minified build, which this book's CI does not run.
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
    implementation(libs.koin.android)
}
