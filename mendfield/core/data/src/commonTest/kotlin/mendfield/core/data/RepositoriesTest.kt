// mendfield/core/data/src/commonTest/kotlin/mendfield/core/data/RepositoriesTest.kt
package mendfield.core.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mendfield.core.model.ItemCategory
import mendfield.core.model.OutcomeKind
import mendfield.core.sync.Operation
import mendfield.core.testing.SampleData
import mendfield.core.testing.TestCafe

class RepositoriesTest {
    @Test fun aRefreshFillsTheCacheAndTheBoardReadsIt() = runTest {
        val cafe = TestCafe()
        assertNull(cafe.events.board("evt-02").first()) // nothing yet
        cafe.loadEverything()
        val board = cafe.events.board("evt-02").first()!!
        assertEquals(SampleData.november, board.event)
        assertEquals(SampleData.novemberData, board.data)
    }

    @Test fun aFailedRefreshKeepsWhatTheDeviceKnows() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        assertEquals(DataResult.Failure(DataError.Offline), cafe.events.refreshEvents())
        assertEquals(DataResult.Failure(DataError.Offline),
            cafe.events.refreshEvent("evt-02"))
        assertEquals(SampleData.events, cafe.events.events.first())
    }

    @Test fun writesGoToTheCacheAndTheOutboxWhileOffline() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        val item = cafe.intake.registerItem("evt-02", ItemCategory.LAMP,
            "  Lamp hums ", "Lin Okoro") as WriteResult.Saved
        assertEquals("Lamp hums", item.value.description) // trimmed
        cafe.intake.assign("evt-02.fx-01.2", item.value.id)
        cafe.outcomes.logOutcome(item.value.id, OutcomeKind.FIXED, "New switch")

        val board = cafe.events.board("evt-02").first()!!
        assertEquals(item.value.id, board.slots.first { it.id == "evt-02.fx-01.2" }.itemId)
        // One id generator: id-1 item, id-2 its operation, id-3 the
        // assignment, id-4 the outcome, id-5 its operation.
        assertEquals(listOf("id-2", "id-3", "id-5"),
            cafe.store.pending.value.map { it.id })
        assertEquals(1, cafe.outcomes.stats("evt-02").first().counts[OutcomeKind.FIXED])
    }

    @Test fun aRefreshKeepsUnsentChanges() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        val item = (cafe.intake.registerItem("evt-02", ItemCategory.TOY,
            "Bear lost an eye", "Ada Mensah") as WriteResult.Saved).value
        cafe.setOnline(true) // the server does not have the item yet
        cafe.events.refreshEvent("evt-02")
        val items = cafe.events.board("evt-02").first()!!.items
        assertEquals(item, items.last())
    }

    @Test fun theRulesRefuseAnAssignmentBeforeAnythingIsQueued() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        val taken = cafe.intake.assign("evt-02.fx-01.1", SampleData.jacket.id)
        assertEquals(WriteResult.Refused("The slot already holds another item."), taken)
        val badForm = cafe.intake.registerItem("evt-02", ItemCategory.LAMP, " ", "Ada")
        assertIs<WriteResult.Refused>(badForm)
        assertEquals(emptyList(), cafe.store.pending.value)
    }

    @Test fun theOutboxIsWrittenBeforeTheCache() = runTest {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.intake.registerItem("evt-02", ItemCategory.CLOCK, "Stopped", "Bo")
        val op = cafe.store.pending.value.single().operation
        assertIs<Operation.RegisterItem>(op)
        assertEquals(op.item, cafe.cache.item(op.item.id))
    }
}
