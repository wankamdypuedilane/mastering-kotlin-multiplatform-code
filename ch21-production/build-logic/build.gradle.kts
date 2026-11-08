// ch21-production/build-logic/build.gradle.kts
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
