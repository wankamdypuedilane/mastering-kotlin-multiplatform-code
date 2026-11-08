// mendfield/core/database/src/jvmTest/kotlin/mendfield/core/database/FileDatabaseTest.kt
package mendfield.core.database

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mendfield.core.testing.OutboxStoreContract
import mendfield.core.testing.SampleData

/** The real builder of the desktop app, on a file: close, open, still there. */
class FileDatabaseTest {
    @Test fun theBoardAndTheOutboxSurviveARestart() = runTest {
        val file = Files.createTempDirectory("mendfield").resolve("m.db").toFile()
        val first = mendfieldDatabase(file)
        RoomCafeCache(first).replaceEventData("evt-02", SampleData.novemberData)
        RoomOutboxStore(first).add(OutboxStoreContract.op("a", 1))
        first.close()

        val second = mendfieldDatabase(file)
        assertEquals(SampleData.novemberData,
            RoomCafeCache(second).eventData("evt-02").first())
        assertEquals(listOf(OutboxStoreContract.op("a", 1)),
            RoomOutboxStore(second).pending.first())
        second.close()
        file.parentFile.deleteRecursively()
    }
}
