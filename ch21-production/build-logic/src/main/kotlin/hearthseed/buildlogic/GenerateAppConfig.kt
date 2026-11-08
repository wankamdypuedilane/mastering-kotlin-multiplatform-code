// ch21-production/build-logic/src/main/kotlin/hearthseed/buildlogic/GenerateAppConfig.kt
package hearthseed.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/** Writes `<objectName>.kt` with one constant per public build property. */
@CacheableTask
abstract class GenerateAppConfig : DefaultTask() {
    @get:Input abstract val packageName: Property<String>
    @get:Input abstract val objectName: Property<String>
    @get:Input abstract val entries: MapProperty<String, String>
    @get:OutputDirectory abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val pkg = packageName.get()
        val dir = outputDir.get().asFile.resolve(pkg.replace('.', '/'))
        dir.deleteRecursively()
        dir.mkdirs()
        val name = objectName.get()
        val source = AppConfigRenderer.render(pkg, name, entries.get())
        dir.resolve("$name.kt").writeText(source)
    }
}
