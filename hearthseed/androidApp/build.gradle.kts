// hearthseed/androidApp/build.gradle.kts
plugins {
    id("hearthseed.android-application") // compileSdk 37, minSdk 24, targetSdk 36
}

android {
    namespace = "hearthseed.android"
    defaultConfig {
        applicationId = "dev.example.hearthseed"
        versionCode = 1
        versionName = "0.17.0"
        // The emulator's alias of the host, where hearthseed-server runs.
        buildConfigField("String", "BASE_URL",
            "\"http://10.0.2.2:8085/api/v1/\"")
    }
    buildFeatures {
        buildConfig = true // off by default since AGP 8.0
    }
    buildTypes {
        // Exercise 17.2: a real phone reaches the host through
        // `adb reverse tcp:8085 tcp:8085`, so it uses localhost.
        create("device") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".device"
            versionNameSuffix = "-device"
            buildConfigField("String", "BASE_URL",
                "\"http://localhost:8085/api/v1/\"")
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true // Robolectric reads res/
        // Robolectric 4.17 with Android 16 (API 36) on JDK 21 reads a JDK
        // internal (found by building: IllegalAccessException without it).
        unitTests.all {
            it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.activity.compose)
    implementation(libs.koin.android)

    // Host tests (src/test): Android framework on the JVM through Robolectric.
    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
