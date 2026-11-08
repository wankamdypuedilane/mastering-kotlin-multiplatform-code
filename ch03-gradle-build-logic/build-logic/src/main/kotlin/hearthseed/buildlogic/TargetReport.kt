// ch03-gradle-build-logic/build-logic/src/main/kotlin/.../TargetReport.kt
package hearthseed.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** Lists the Kotlin targets of a module. No Project access at execution. */
abstract class TargetReport : DefaultTask() {
    @get:Input
    abstract val modulePath: Property<String>

    @get:Input
    abstract val targets: ListProperty<String>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun write() {
        val lines = listOf("Targets of ${modulePath.get()}:") +
            targets.get().map { "  - $it" }
        reportFile.get().asFile.writeText(lines.joinToString("\n") + "\n")
        lines.forEach { logger.lifecycle(it) }
    }
}
