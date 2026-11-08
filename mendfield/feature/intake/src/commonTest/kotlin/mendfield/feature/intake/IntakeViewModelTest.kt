// mendfield/feature/intake/src/commonTest/kotlin/mendfield/feature/intake/IntakeViewModelTest.kt
package mendfield.feature.intake

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mendfield.core.data.DraftProblem
import mendfield.core.model.ItemCategory
import mendfield.core.testing.TestCafe

@OptIn(ExperimentalCoroutinesApi::class)
class IntakeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun triageHintsFollowTheCategory() = runTest(dispatcher) {
        val cafe = TestCafe()
        cafe.loadEverything()
        val vm = IntakeViewModel("evt-02", cafe.events, cafe.intake, dispatcher)
        backgroundScope.launch(dispatcher) { vm.state.collect {} }
        vm.onCategory(ItemCategory.LAMP)
        advanceUntilIdle()
        // Maud has the lamp already; nobody else does electrics.
        assertEquals(listOf("Maud Okafor: best fit, 1 booked"),
            vm.state.value.hints.map { it.text })
        vm.onCategory(ItemCategory.TOY) // mechanical first, then sewing
        advanceUntilIdle()
        assertEquals(listOf("fx-02", "fx-03", "fx-01"),
            vm.state.value.hints.map { it.fixerId })
    }

    @Test fun problemsShowOnlyAfterTheFirstSave() = runTest(dispatcher) {
        val cafe = TestCafe()
        cafe.loadEverything()
        val vm = IntakeViewModel("evt-02", cafe.events, cafe.intake, dispatcher)
        backgroundScope.launch(dispatcher) { vm.state.collect {} }
        vm.onCategory(ItemCategory.CLOCK)
        advanceUntilIdle()
        assertEquals(emptySet(), vm.state.value.problems)
        vm.save()
        advanceUntilIdle()
        assertEquals(setOf(DraftProblem.DESCRIPTION_EMPTY, DraftProblem.VISITOR_EMPTY),
            vm.state.value.problems)
        assertEquals(emptyList(), cafe.store.pending.value)
    }

    @Test fun savingWorksOfflineAndReportsTheNewId() = runTest(dispatcher) {
        val cafe = TestCafe()
        cafe.loadEverything()
        cafe.setOnline(false)
        val vm = IntakeViewModel("evt-02", cafe.events, cafe.intake, dispatcher)
        backgroundScope.launch(dispatcher) { vm.state.collect {} }
        vm.onCategory(ItemCategory.CLOCK)
        vm.onDescription("Cuckoo is silent")
        vm.onVisitorName("Gwen Arbuthnot")
        vm.save()
        advanceUntilIdle()
        val id = assertNotNull(vm.state.value.savedItemId)
        assertEquals("Cuckoo is silent", cafe.cache.item(id)?.description)
        assertEquals(1, cafe.store.pending.value.size)
    }
}
