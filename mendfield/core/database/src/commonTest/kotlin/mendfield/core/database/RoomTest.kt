// mendfield/core/database/src/commonTest/kotlin/mendfield/core/database/RoomTest.kt
package mendfield.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import mendfield.core.model.Skill
import mendfield.core.testing.CafeCacheContract
import mendfield.core.testing.OutboxStoreContract
import mendfield.core.testing.SampleData

// commonTest of this module runs on the JVM and the iOS simulator, with the
// same bundled SQLite as the apps. A fresh in-memory database per test.
class RoomCafeCacheTest {
    private val db = inMemoryDatabase()
    private val cache = RoomCafeCache(db)

    @AfterTest fun close() = db.close()

    @Test fun orders() = runTest { CafeCacheContract.keepsTheDocumentedOrders(cache) }
    @Test fun fields() = runTest { CafeCacheContract.storesEveryFieldExactly(cache) }
    @Test fun replaceOneEvent() = runTest {
        CafeCacheContract.replacingOneEventKeepsTheOthers(cache)
    }
    @Test fun replaceEvents() = runTest {
        CafeCacheContract.replacingEventsDropsTheGoneOnes(cache)
    }
    @Test fun localChanges() = runTest {
        CafeCacheContract.localChangesShowUpInTheFlows(cache)
    }

    @Test fun aReplaceIsOneEmissionNotAHalfReplacedBoard() = runTest {
        cache.replaceEventData("evt-02", SampleData.novemberData)
        val sizes = Channel<Int>(Channel.UNLIMITED)
        val job = launch { cache.eventData("evt-02").collect { sizes.send(it.items.size) } }
        assertEquals(2, sizes.receive()) // the first read, on Room's thread
        cache.replaceEventData("evt-02", SampleData.novemberData.copy(
            items = SampleData.novemberData.items.take(1)))
        // Delete-then-insert happens in one transaction: the next value is
        // the new board, never the empty one in between.
        assertEquals(1, sizes.receive())
        job.cancel()
    }
}

class RoomOutboxStoreTest {
    private val db = inMemoryDatabase()

    @AfterTest fun close() = db.close()

    @Test fun order() = runTest {
        OutboxStoreContract.keepsArrivalOrderAndEveryField(RoomOutboxStore(db))
    }
    @Test fun removeAndReject() = runTest {
        OutboxStoreContract.removesAndRejects(RoomOutboxStore(db))
    }
    @Test fun aSecondStoreOnTheSameDatabaseSeesTheQueue() = runTest {
        OutboxStoreContract.survivesARestart(RoomOutboxStore(db)) { RoomOutboxStore(db) }
    }
}

class ConvertersTest {
    @Test fun instantsKeepTheirNanoseconds() {
        val instants = listOf(Instant.parse("2026-11-08T07:17:21.972099053Z"),
            Instant.parse("1969-12-31T23:59:59.5Z"), Instant.fromEpochSeconds(0))
        instants.forEach {
            assertEquals(it, Converters.nanosToInstant(Converters.instantToNanos(it)))
        }
        assertEquals(instants.sorted(), instants.sortedBy { Converters.instantToNanos(it) })
    }

    @Test fun skillsRoundTripAndUnknownNamesAreSkipped() {
        val skills = setOf(Skill.SEWING, Skill.ELECTRICAL)
        assertEquals("ELECTRICAL,SEWING", Converters.skillsToText(skills))
        assertEquals(skills, Converters.textToSkills("ELECTRICAL,SEWING,WELDING"))
        assertEquals(emptySet(), Converters.textToSkills(""))
    }
}

fun inMemoryDatabase(): MendfieldDatabase =
    Room.inMemoryDatabaseBuilder<MendfieldDatabase>()
        .setDriver(BundledSQLiteDriver()) // the same SQLite as the apps
        .build()
