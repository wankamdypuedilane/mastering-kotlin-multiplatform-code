// ch21-production/build-logic/src/main/kotlin/hearthseed.app-config.gradle.kts
import hearthseed.buildlogic.GenerateAppConfig
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

interface AppConfigExtension {
    val packageName: Property<String>
}

val appConfig = extensions.create<AppConfigExtension>("appConfig")

val generateAppConfig = tasks.register<GenerateAppConfig>("generateAppConfig") {
    packageName = appConfig.packageName
    objectName = "AppConfig"
    // Every `app.config.<name>` Gradle property, prefix removed.
    entries = providers.gradlePropertiesPrefixedBy("app.config.").map { all ->
        all.mapKeys { (key, _) -> key.removePrefix("app.config.") }
    }
    outputDir = layout.buildDirectory.dir("generated/appConfig/commonMain")
}

pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        // Passing the task's output also wires the task dependency.
        sourceSets.getByName("commonMain").kotlin
            .srcDir(generateAppConfig.map { it.outputDir })
    }
}
