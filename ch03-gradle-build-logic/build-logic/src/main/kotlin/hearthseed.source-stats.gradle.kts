// ch03-gradle-build-logic/build-logic/.../hearthseed.source-stats.gradle.kts
// Exercise 3.3: registers a cacheable, configuration-cache-safe report.
import hearthseed.buildlogic.SourceStats

tasks.register<SourceStats>("sourceStats") {
    group = "help"
    description = "Counts Kotlin files and lines per source set."
    sourceRoot.set(layout.projectDirectory.dir("src"))
    reportFile.set(layout.buildDirectory.file("reports/source-stats.txt"))
}
