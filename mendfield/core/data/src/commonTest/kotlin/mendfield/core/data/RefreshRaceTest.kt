// mendfield/core/data/src/commonTest/kotlin/mendfield/core/data/RefreshRaceTest.kt
package mendfield.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import mendfield.core.model.ItemCategory
import mendfield.core.network.ApiResult
import mendfield.core.testing.TestCafe

/** Answers a read at once, but hands the answer over only when told to. */
private class SlowReads(private val real: CafeRemote) : CafeRemote by real {
    val gate = CompletableDeferred<Unit>()
    override suspend fun eventData(eventId: String): ApiResult<EventData> {
        val answer = real.eventData(eventId) // the server's state now
        gate.await()                         // ... the response is slow
        return answer
    }
}

class RefreshRaceTest {
    @Test fun aSyncDuringARefreshDoesNotDropTheDeliveredItem() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        val item = (cafe.intake.registerItem("evt-02", ItemCategory.TOY,
            "Bear lost an eye", "Ada Mensah") as WriteResult.Saved).value
        cafe.setOnline(true)

        // 1. A refresh reads the event before the server has the item.
        val slow = SlowReads(cafe.server)
        val events = EventRepository(slow, cafe.cache, cafe.writer,
            Dispatchers.Unconfined)
        val refresh = async { events.refreshEvent("evt-02") }
        runCurrent()
        // 2. Meanwhile the engine delivers the item and empties the queue.
        cafe.engine.syncNow()
        assertTrue(cafe.server.data.getValue("evt-02").items.any { it.id == item.id })
        assertEquals(emptyList(), cafe.store.pending.value)
        // 3. The stale answer arrives: it lacks the item, the queue too.
        slow.gate.complete(Unit)
        refresh.await()

        val items = cafe.events.board("evt-02").first()!!.items
        assertEquals(item, items.last()) // rebased on the queue of step 1
    }
}
