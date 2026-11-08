// ch03-gradle-build-logic/build-logic/src/main/kotlin/.../SourceStats.kt
package hearthseed.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Exercise 3.3: Kotlin files and lines per source set (src/<set>/kotlin). */
@CacheableTask
abstract class SourceStats : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceRoot: DirectoryProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun count() {
        val root = sourceRoot.get().asFile
        val lines = root.listFiles().orEmpty().filter { it.isDirectory }
            .sortedBy { it.name }
            .map { set ->
                val files = set.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }.toList()
                val count = files.sumOf { it.readLines().size }
                statLine(set.name, files.size, count)
            }
        reportFile.get().asFile.writeText(lines.joinToString("\n") + "\n")
        lines.forEach { logger.lifecycle(it) }
    }
}

/** One report line: "commonMain: 1 file, 7 lines". */
internal fun statLine(sourceSet: String, files: Int, lines: Int): String =
    "$sourceSet: ${plural(files, "file")}, ${plural(lines, "line")}"

internal fun plural(count: Int, noun: String): String =
    if (count == 1) "$count $noun" else "$count ${noun}s"
