// mendfield/core/testing/src/commonMain/kotlin/mendfield/core/testing/OutboxStoreContract.kt
package mendfield.core.testing

import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import mendfield.core.model.OutcomeKind
import mendfield.core.model.Outcome
import mendfield.core.sync.Operation
import mendfield.core.sync.OutboxStore
import mendfield.core.sync.PendingOperation
import mendfield.core.sync.RejectedOperation

/** What every persistent [OutboxStore] must do (Room, localStorage). */
object OutboxStoreContract {
    fun op(id: String, minute: Int = 0): PendingOperation = PendingOperation(
        id, SampleData.at(minute),
        when (minute % 3) {
            0 -> Operation.AssignSlot("evt-02.fx-01.2", "item-$id")
            1 -> Operation.RegisterItem(SampleData.jacket.copy(id = "item-$id"))
            else -> Operation.LogOutcome(Outcome("out-$id", "item-001",
                OutcomeKind.PARTIAL, "Needs a part", SampleData.at(minute)))
        },
    )

    suspend fun keepsArrivalOrderAndEveryField(store: OutboxStore) {
        val ops = listOf(op("c", 0), op("a", 1), op("b", 2))
        ops.forEach { store.add(it) }
        store.add(op("a", 1)) // a duplicate id is ignored
        assertEquals(ops, store.pending.first())
        assertEquals(ops.take(2), store.oldest(2))
    }

    suspend fun removesAndRejects(store: OutboxStore) {
        listOf("a", "b", "c").forEach { store.add(op(it)) }
        store.remove(setOf("a"))
        store.reject(listOf(RejectedOperation(op("b"), "slot_taken", "Taken")))
        assertEquals(listOf(op("c")), store.pending.first())
        assertEquals(listOf(RejectedOperation(op("b"), "slot_taken", "Taken")),
            store.rejected.first())
        store.dismiss("b")
        assertEquals(emptyList(), store.rejected.first())
    }

    /** [reopen] gives a new store over the same storage, like an app restart. */
    suspend fun survivesARestart(first: OutboxStore, reopen: () -> OutboxStore) {
        first.add(op("a", 1))
        first.add(op("b", 2))
        first.reject(listOf(RejectedOperation(op("a", 1), "id_conflict", "No")))
        val second = reopen()
        assertEquals(listOf(op("b", 2)), second.pending.first())
        assertEquals(listOf("a"), second.rejected.first().map { it.pending.id })
    }
}
