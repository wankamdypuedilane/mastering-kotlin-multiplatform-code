// hearthseed/build-logic/src/main/kotlin/hearthseed.module-rules.gradle.kts
// Exercise 11.4: apply to the root project; run ./gradlew checkModuleRules.
import hearthseed.buildlogic.CheckModuleRules

// Read lazily, when the task graph is built: every module is configured then.
val mainDependencies = provider {
    subprojects.associate { module ->
        module.path to module.configurations
            .filterNot { it.name.contains("test", ignoreCase = true) }
            .flatMap { it.dependencies.withType<ProjectDependency>() }
            .map { it.path }.filter { it != module.path }.distinct().sorted()
    }
}

tasks.register<CheckModuleRules>("checkModuleRules") {
    group = "verification"
    description = "Checks the dependency rules between Hearthseed modules."
    dependencies.set(mainDependencies)
    reportFile.set(layout.buildDirectory.file("reports/module-rules.txt"))
}
