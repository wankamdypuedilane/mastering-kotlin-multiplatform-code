// mendfield/core/sync/src/commonTest/kotlin/mendfield/core/sync/OutboxStoreTest.kt
package mendfield.core.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

class OutboxStoreTest {
    @Test fun keepsOperationsInArrivalOrder() = runTest {
        val store = InMemoryOutboxStore()
        listOf("c", "a", "b").forEach { store.add(pending(it)) }
        assertEquals(listOf("c", "a"), store.oldest(2).map { it.id })
        assertEquals(listOf("c", "a", "b"), store.pending.value.map { it.id })
    }

    @Test fun addingTheSameIdTwiceKeepsOneCopy() = runTest {
        val store = InMemoryOutboxStore()
        store.add(pending("a"))
        store.add(pending("a"))
        assertEquals(1, store.pending.value.size)
    }

    @Test fun rejectMovesOperationsBetweenTheLists() = runTest {
        val store = InMemoryOutboxStore()
        store.add(pending("a"))
        store.add(pending("b"))
        store.reject(listOf(RejectedOperation(pending("a"), "slot_taken", "Taken")))
        assertEquals(listOf("b"), store.pending.value.map { it.id })
        assertEquals(listOf("a"), store.rejected.value.map { it.pending.id })
        store.dismiss("a")
        assertEquals(emptyList(), store.rejected.value)
    }

    @Test fun removeForgetsConfirmedOperations() = runTest {
        val store = InMemoryOutboxStore()
        listOf("a", "b", "c").forEach { store.add(pending(it)) }
        store.remove(setOf("a", "c"))
        assertEquals(listOf("b"), store.pending.value.map { it.id })
    }

    @Test fun theOutboxStampsIdsAndTimes() = runTest {
        val store = InMemoryOutboxStore()
        val clock = FixedClock()
        var n = 0
        val outbox = Outbox(store, clock) { "id-${++n}" }
        val itemId = outbox.newEntityId()
        clock.now = T0 + 5.seconds
        val queued = outbox.enqueue(Operation.RegisterItem(item(itemId)))
        assertEquals(PendingOperation("id-2", T0 + 5.seconds,
            Operation.RegisterItem(item("id-1"))), queued)
        assertEquals(listOf(queued), store.pending.value)
    }

    @Test fun theDefaultIdsAreRandomUuids() = runTest {
        val outbox = Outbox(InMemoryOutboxStore())
        val ids = List(100) { outbox.newEntityId() }.toSet()
        assertEquals(100, ids.size)
        assertEquals(36, ids.first().length) // 8-4-4-4-12 hex digits
    }
}
