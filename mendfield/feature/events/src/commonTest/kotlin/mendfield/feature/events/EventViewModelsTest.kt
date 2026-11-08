// mendfield/feature/events/src/commonTest/kotlin/mendfield/feature/events/EventViewModelsTest.kt
package mendfield.feature.events

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mendfield.core.data.DataError
import mendfield.core.testing.SampleData
import mendfield.core.testing.TestCafe

@OptIn(ExperimentalCoroutinesApi::class)
class EventViewModelsTest {
    private val dispatcher = StandardTestDispatcher()

    // viewModelScope runs on Dispatchers.Main: the test's dispatcher here.
    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    /** WhileSubscribed state flows compute only while someone collects. */
    private fun <T> TestScope.collect(flow: kotlinx.coroutines.flow.Flow<T>) {
        backgroundScope.launch(dispatcher) { flow.collect {} }
    }

    @Test fun theListRefreshesOnOpenAndShowsTheCache() = runTest(dispatcher) {
        val cafe = TestCafe()
        val vm = EventListViewModel(cafe.events)
        collect(vm.state)
        advanceUntilIdle()
        val state = vm.state.value
        assertEquals(listOf("evt-01", "evt-02"), state.events.map { it.id })
        assertEquals("Sat 21 Nov 2026, 10:00–12:00", state.events[1].whenText)
        assertNull(state.refreshError)
    }

    @Test fun offlineTheListKeepsItsRowsAndSaysWhy() = runTest(dispatcher) {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        val vm = EventListViewModel(cafe.events)
        collect(vm.state)
        advanceUntilIdle()
        assertEquals(2, vm.state.value.events.size)
        assertEquals(DataError.Offline, vm.state.value.refreshError)
        cafe.setOnline(true)
        vm.refresh() // the Retry button
        advanceUntilIdle()
        assertNull(vm.state.value.refreshError)
    }

    @Test fun anUnknownEventOfflineIsMissingWithTheReason() = runTest(dispatcher) {
        val cafe = TestCafe(online = false)
        val vm = EventBoardViewModel("evt-02", cafe.events, cafe.intake, cafe.clock, dispatcher)
        collect(vm.state)
        advanceUntilIdle()
        assertEquals(BoardStatus.Missing(DataError.Offline), vm.state.value.status)
    }

    @Test fun selectingAnItemProposesSlotsAndAssignmentMovesIt() = runTest(dispatcher) {
        val cafe = TestCafe()
        cafe.clock.now = SampleData.at(-120) // before the café opens
        val vm = EventBoardViewModel("evt-02", cafe.events, cafe.intake, cafe.clock, dispatcher)
        collect(vm.state)
        advanceUntilIdle()
        vm.select(SampleData.jacket.id)
        advanceUntilIdle()
        val ready = assertIs<BoardStatus.Ready>(vm.state.value.status).content
        assertEquals(BoardCounts(registered = 2, waiting = 1, scheduled = 1,
            withOutcome = 0), ready.counts)
        val first = ready.selection!!.suggestions.first()
        assertEquals("evt-02.fx-03.1", first.slotId) // Ines sews
        assertEquals("Ines Halloran at 10:00 (best fit, 0 booked)", first.text)

        vm.assign(first.slotId)
        advanceUntilIdle()
        val after = assertIs<BoardStatus.Ready>(vm.state.value.status).content
        assertEquals(0, after.counts.waiting)
        assertNull(after.selection)
        val ines = after.schedule.first { it.fixerId == "fx-03" }
        assertEquals("Broken zip (Kofi Asante)", ines.slots.first().itemText)
    }

    @Test fun aRefusedAssignmentBecomesAMessage() = runTest(dispatcher) {
        val cafe = TestCafe()
        val vm = EventBoardViewModel("evt-02", cafe.events, cafe.intake, cafe.clock, dispatcher)
        collect(vm.state)
        advanceUntilIdle()
        vm.select(SampleData.jacket.id)
        vm.assign("evt-02.fx-01.1") // holds the lamp
        advanceUntilIdle()
        assertEquals("The slot already holds another item.", vm.state.value.message)
        vm.messageShown()
        advanceUntilIdle()
        assertNull(vm.state.value.message)
    }
}
