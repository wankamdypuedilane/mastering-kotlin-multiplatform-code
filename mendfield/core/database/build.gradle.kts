// mendfield/core/database/build.gradle.kts
// Room 3 behind CafeCache and OutboxStore: the device copy and the outbox,
// in one SQLite file, on Android, iOS and the desktop.
plugins {
    id("mendfield.kmp-room") // android, jvm, iOS; KSP; schemas/
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))   // CafeCache, EventData
            api(project(":core:sync"))   // OutboxStore, OperationCodec
        }
        commonTest.dependencies {
            implementation(project(":core:testing")) // the shared contracts
        }
    }
}
