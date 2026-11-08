// mendfield/core/testing/src/commonMain/kotlin/mendfield/core/testing/CafeCacheContract.kt
package mendfield.core.testing

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import mendfield.core.data.CafeCache
import mendfield.core.data.EventData
import mendfield.core.model.Outcome
import mendfield.core.model.OutcomeKind

/**
 * What every [CafeCache] must do, as plain suspend functions: each
 * implementation's test class calls them from its own @Test functions, which
 * works the same on every target (no inherited tests across modules).
 */
object CafeCacheContract {
    suspend fun keepsTheDocumentedOrders(cache: CafeCache) {
        cache.replaceEvents(SampleData.events.reversed())
        cache.replaceFixers(SampleData.fixers.reversed())
        cache.replaceEventData("evt-02", SampleData.novemberData.let {
            it.copy(slots = it.slots.reversed(), items = it.items.reversed())
        })
        assertEquals(SampleData.events, cache.events().first())
        assertEquals(SampleData.fixers, cache.fixers().first())
        assertEquals(SampleData.novemberData, cache.eventData("evt-02").first())
    }

    suspend fun storesEveryFieldExactly(cache: CafeCache) {
        cache.replaceEvents(SampleData.events)
        cache.replaceEventData("evt-01", SampleData.octoberData)
        assertEquals(SampleData.october, cache.event("evt-01"))
        assertEquals(SampleData.toaster, cache.item(SampleData.toaster.id))
        assertEquals(SampleData.octoberData, cache.eventData("evt-01").first())
        assertNull(cache.event("evt-99"))
    }

    suspend fun replacingOneEventKeepsTheOthers(cache: CafeCache) {
        cache.replaceEventData("evt-01", SampleData.octoberData)
        cache.replaceEventData("evt-02", SampleData.novemberData)
        val smaller = EventData(slots = SampleData.novemberSlots.take(2))
        cache.replaceEventData("evt-02", smaller)
        assertEquals(smaller, cache.eventData("evt-02").first())
        assertEquals(SampleData.octoberData, cache.eventData("evt-01").first())
        assertNull(cache.item(SampleData.lamp.id)) // gone with its event
    }

    suspend fun replacingEventsDropsTheGoneOnes(cache: CafeCache) {
        cache.replaceEvents(SampleData.events)
        cache.replaceEvents(listOf(SampleData.november))
        assertEquals(listOf(SampleData.november), cache.events().first())
    }

    suspend fun localChangesShowUpInTheFlows(cache: CafeCache) {
        cache.replaceEventData("evt-02", SampleData.novemberData)
        val jacket = SampleData.jacket
        cache.assign("evt-02.fx-03.2", jacket.id)
        val note = Outcome("out-9", jacket.id, OutcomeKind.FIXED, "New zip",
            SampleData.at(70))
        cache.putOutcome(note)
        val newItem = jacket.copy(id = "item-003", visitorName = "Lin")
        cache.putItem(newItem)
        val data = cache.eventData("evt-02").first()
        assertEquals(jacket.id, data.slots.first { it.id == "evt-02.fx-03.2" }.itemId)
        assertEquals(listOf(note), data.outcomes)
        assertEquals(listOf("item-001", "item-002", "item-003"), data.items.map { it.id })
        assertEquals(newItem, cache.item("item-003"))
    }
}
