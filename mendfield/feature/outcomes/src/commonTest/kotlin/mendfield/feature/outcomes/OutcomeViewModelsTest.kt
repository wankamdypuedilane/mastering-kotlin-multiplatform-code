// mendfield/feature/outcomes/src/commonTest/kotlin/mendfield/feature/outcomes/OutcomeViewModelsTest.kt
package mendfield.feature.outcomes

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mendfield.core.model.OutcomeKind
import mendfield.core.testing.SampleData
import mendfield.core.testing.TestCafe

@OptIn(ExperimentalCoroutinesApi::class)
class OutcomeViewModelsTest {
    private val dispatcher = StandardTestDispatcher()
    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun loggingAnOutcomeUpdatesHistoryAndStats() = runTest(dispatcher) {
        val cafe = TestCafe()
        cafe.loadEverything()
        val log = LogOutcomeViewModel(SampleData.lamp.id, cafe.outcomes)
        val stats = EventStatsViewModel("evt-02", cafe.events, cafe.outcomes)
        backgroundScope.launch(dispatcher) { log.state.collect {} }
        backgroundScope.launch(dispatcher) { stats.state.collect {} }
        advanceUntilIdle()
        val item = assertIs<ItemStatus.Ready>(log.state.value.item).summary
        assertEquals("Maud Okafor, 10:00–10:30", item.slot)
        assertEquals("No repairs tried yet", stats.state.value.headline)
        assertFalse(log.state.value.canSave) // no kind chosen

        cafe.clock.now = SampleData.at(25)
        log.onKind(OutcomeKind.PARTIAL)
        log.onNote("Needs a new switch")
        log.save()
        advanceUntilIdle()
        assertTrue(log.state.value.saved)
        assertEquals(listOf("10:25 Partly fixed: Needs a new switch"),
            (log.state.value.item as ItemStatus.Ready).summary.history)
        assertEquals("100 % repaired", stats.state.value.headline)

        cafe.clock.now += 5.minutes // a later outcome wins in the statistics
        val second = LogOutcomeViewModel(SampleData.lamp.id, cafe.outcomes)
        backgroundScope.launch(dispatcher) { second.state.collect {} }
        advanceUntilIdle()
        second.onKind(OutcomeKind.NOT_FIXED)
        second.save()
        advanceUntilIdle()
        assertEquals("0 % repaired", stats.state.value.headline)
        assertEquals(2, cafe.store.pending.value.size)
    }

    @Test fun anUnknownItemIsMissing() = runTest(dispatcher) {
        val vm = LogOutcomeViewModel("nope", TestCafe().outcomes)
        backgroundScope.launch(dispatcher) { vm.state.collect {} }
        advanceUntilIdle()
        assertEquals(ItemStatus.Missing, vm.state.value.item)
    }

    @Test fun aTooLongNoteCannotBeSaved() {
        val state = LogOutcomeUiState(
            item = ItemStatus.Ready(ItemSummary("t", "v", "s", emptyList())),
            kind = OutcomeKind.ADVICE, note = "x".repeat(501))
        assertTrue(state.noteTooLong)
        assertFalse(state.canSave)
    }
}
