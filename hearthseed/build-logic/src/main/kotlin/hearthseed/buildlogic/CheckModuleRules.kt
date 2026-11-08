// hearthseed/build-logic/src/main/kotlin/hearthseed/buildlogic/CheckModuleRules.kt
package hearthseed.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** Fails the build when a module dependency breaks [moduleRuleViolations]. */
abstract class CheckModuleRules : DefaultTask() {
    @get:Input
    abstract val dependencies: MapProperty<String, List<String>>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun check() {
        val edges = dependencies.get().mapValues { it.value.toSet() }
        val violations = moduleRuleViolations(edges)
        val report = edges.toSortedMap().map { (from, to) -> "$from -> ${to.sorted()}" }
        reportFile.get().asFile.writeText((report + violations).joinToString("\n") + "\n")
        if (violations.isNotEmpty()) {
            throw GradleException(violations.joinToString("\n", "Module rules broken:\n"))
        }
        logger.lifecycle("Module rules hold for ${edges.size} modules.")
    }
}
