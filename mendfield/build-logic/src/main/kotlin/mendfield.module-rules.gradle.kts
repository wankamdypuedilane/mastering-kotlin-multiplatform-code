// mendfield/build-logic/src/main/kotlin/mendfield.module-rules.gradle.kts
// Apply to the root project; ./gradlew checkModuleRules checks the graph.
import mendfield.buildlogic.CheckModuleRules

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
    description = "Checks the dependency rules between Mendfield modules."
    dependencies.set(mainDependencies)
    reportFile.set(layout.buildDirectory.file("reports/module-rules.txt"))
}
