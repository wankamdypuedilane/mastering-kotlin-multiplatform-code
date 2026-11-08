package hearthseed.platform

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Where an export goes and the header line it starts with. */
data class ExportPlan(val path: String?, val header: String)

/** Common logic: every platform-dependent input is passed in. */
class ExportPlanner(
    private val platform: PlatformInfo,
    private val directories: AppDirectories,
    private val clock: Clock = Clock.System,
    private val ids: IdGenerator = UuidIdGenerator,
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    fun plan(kind: String): ExportPlan {
        val now = clock.now()
        val day = now.toLocalDateTime(timeZone).date      // e.g. 2026-11-08
        val fileName = "$kind-$day-${ids.newId().take(8)}.csv"
        val path = directories.data?.let { joinPath(it, "exports", fileName) }
        val header = "# $kind exported $now from ${platform.summary}"
        return ExportPlan(path, header)
    }
}
