// mendfield/core/data/src/commonTest/kotlin/mendfield/core/data/InMemoryStoresTest.kt
package mendfield.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import mendfield.core.testing.CafeCacheContract
import mendfield.core.testing.OutboxStoreContract

class InMemoryCafeCacheTest {
    @Test fun orders() = runTest {
        CafeCacheContract.keepsTheDocumentedOrders(InMemoryCafeCache())
    }
    @Test fun fields() = runTest {
        CafeCacheContract.storesEveryFieldExactly(InMemoryCafeCache())
    }
    @Test fun replaceOneEvent() = runTest {
        CafeCacheContract.replacingOneEventKeepsTheOthers(InMemoryCafeCache())
    }
    @Test fun replaceEvents() = runTest {
        CafeCacheContract.replacingEventsDropsTheGoneOnes(InMemoryCafeCache())
    }
    @Test fun localChanges() = runTest {
        CafeCacheContract.localChangesShowUpInTheFlows(InMemoryCafeCache())
    }
}

class KeyValueOutboxStoreTest {
    @Test fun order() = runTest {
        OutboxStoreContract.keepsArrivalOrderAndEveryField(
            KeyValueOutboxStore(InMemoryStorage()))
    }
    @Test fun removeAndReject() = runTest {
        OutboxStoreContract.removesAndRejects(KeyValueOutboxStore(InMemoryStorage()))
    }
    @Test fun restart() = runTest {
        val storage = InMemoryStorage() // stands for localStorage
        OutboxStoreContract.survivesARestart(KeyValueOutboxStore(storage)) {
            KeyValueOutboxStore(storage)
        }
    }
    @Test fun anUnreadableValueStartsAnEmptyQueue() = runTest {
        val storage = InMemoryStorage()
        storage.write("mendfield.outbox", "{not json")
        val store = KeyValueOutboxStore(storage)
        assertEquals(emptyList(), store.pending.value)
        store.add(OutboxStoreContract.op("a"))
        assertEquals(1, KeyValueOutboxStore(storage).pending.value.size)
    }
}
