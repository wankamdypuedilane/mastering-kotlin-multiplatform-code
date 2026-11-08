package hearthseed.platform.solutions

import hearthseed.platform.AppDirectories
import hearthseed.platform.ExportPlanner
import hearthseed.platform.PlatformInfo
import hearthseed.platform.PlatformKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class SolutionsTest {
    private val start = Instant.parse("2026-11-08T10:00:00Z")

    @Test
    fun stepClockAdvancesOnEveryCall() {
        val clock = StepClock(start, 5.minutes)
        val info = PlatformInfo(PlatformKind.JVM, "Linux", "6.8")
        val none = object : AppDirectories { override val data: String? = null }
        val planner = ExportPlanner(info, none, clock, { "id" }, TimeZone.UTC)
        val first = planner.plan("inventory").header
        val second = planner.plan("inventory").header
        assertTrue("2026-11-08T10:00:00Z" in first, first)
        assertTrue("2026-11-08T10:05:00Z" in second, second)
    }

    @Test
    fun prefixedIdsRoundTrip() {
        val lots = PrefixedIds("lot")
        val id = lots.newId()
        assertTrue(id.startsWith("lot-"))
        assertEquals(id, "lot-${lots.parse(id)}")
        assertNull(lots.parse("member-${id.removePrefix("lot-")}"))
        assertNull(lots.parse("lot-not-a-uuid"))
    }

    @Test
    fun everyPlatformHasAtLeastOneProcessor() {
        assertTrue(processorCount() >= 1)
    }

    @Test
    fun inMemoryStoreRoundTrip() {
        val store = InMemoryTextStore()
        assertNull(store.read("a.csv"))
        store.write("a.csv", "kale;3")
        assertEquals("kale;3", store.read("a.csv"))
    }
}
