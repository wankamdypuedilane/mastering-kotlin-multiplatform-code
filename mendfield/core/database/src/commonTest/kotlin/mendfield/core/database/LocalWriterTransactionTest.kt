// mendfield/core/database/src/commonTest/kotlin/mendfield/core/database/LocalWriterTransactionTest.kt
package mendfield.core.database

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mendfield.core.data.CafeCache
import mendfield.core.data.LocalWriter
import mendfield.core.data.SequentialWrites
import mendfield.core.data.TransactionRunner
import mendfield.core.model.Item
import mendfield.core.sync.Operation
import mendfield.core.testing.SampleData

/** A cache that writes the item, then fails: a full disk, a bug, a crash. */
private class FailingCache(private val real: CafeCache) : CafeCache by real {
    override suspend fun putItem(item: Item) {
        real.putItem(item)
        error("disk full")
    }
}

// The outbox and the cache in one Room transaction: all or nothing.
class LocalWriterTransactionTest {
    private val db = inMemoryDatabase()
    private val store = RoomOutboxStore(db)
    private val cache = RoomCafeCache(db)
    private val register = Operation.RegisterItem(SampleData.lamp)

    @AfterTest fun close() = db.close()

    private fun writer(transactions: TransactionRunner) =
        LocalWriter(FailingCache(cache), store, transactions)

    @Test fun aFailingCacheWriteRollsBackTheOutbox() = runTest {
        assertFailsWith<IllegalStateException> {
            writer(RoomTransactionRunner(db)).write(register)
        }
        assertEquals(emptyList(), store.pending.first()) // rolled back
        assertNull(cache.item(SampleData.lamp.id))       // this one too
    }

    @Test fun withoutATransactionTheOutboxKeepsTheOperation() = runTest {
        assertFailsWith<IllegalStateException> {
            writer(SequentialWrites).write(register)
        }
        // The web's behavior: queued, so the next refresh replays it.
        assertEquals(listOf(register), store.pending.first().map { it.operation })
    }

    @Test fun aSuccessfulWriteCommitsBoth() = runTest {
        LocalWriter(cache, store, RoomTransactionRunner(db)).write(register)
        assertEquals(listOf(register), store.pending.first().map { it.operation })
        assertEquals(SampleData.lamp, cache.item(SampleData.lamp.id))
    }
}
