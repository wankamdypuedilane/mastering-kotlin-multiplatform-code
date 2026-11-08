package hearthseed.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone

/** A clock that always says the same thing. */
class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}

class ExportPlannerTest {
    private val pixel = PlatformInfo(PlatformKind.ANDROID, "Android", "16")
    private val folder = object : AppDirectories {
        override val data = joinPath("data", "hearthseed")
    }
    private val lateEvening = FixedClock(Instant.parse("2026-11-08T23:30:00Z"))
    private val ids = IdGenerator { "0123456789abcdef" }

    @Test
    fun fileNameUsesLocalDateAndShortId() {
        val tokyo = UtcOffset(hours = 9).asTimeZone()   // already Nov 9
        val plan = ExportPlanner(pixel, folder, lateEvening, ids, tokyo)
            .plan("inventory")
        val expected = joinPath(
            "data", "hearthseed", "exports",
            "inventory-2026-11-09-01234567.csv",
        )
        assertEquals(expected, plan.path)
        assertEquals(
            "# inventory exported 2026-11-08T23:30:00Z from Android 16 (android)",
            plan.header,
        )
    }

    @Test
    fun noFileSystemMeansNoPath() {
        val none = object : AppDirectories { override val data: String? = null }
        val plan = ExportPlanner(pixel, none, lateEvening, ids).plan("loans")
        assertNull(plan.path)
    }
}
