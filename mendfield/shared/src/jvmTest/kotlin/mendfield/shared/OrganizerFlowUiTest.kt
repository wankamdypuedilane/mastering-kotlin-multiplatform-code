// mendfield/shared/src/jvmTest/kotlin/mendfield/shared/OrganizerFlowUiTest.kt
package mendfield.shared

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import mendfield.core.data.CafeRemote
import mendfield.core.data.ConnectivityMonitor
import mendfield.core.data.ManualConnectivity
import mendfield.core.data.SyncCoordinator
import mendfield.core.model.OutcomeKind
import mendfield.core.sync.SyncTransport
import mendfield.core.testing.FakeCafeServer
import mendfield.core.testing.SampleData
import mendfield.core.testing.TestClock
import mendfield.shared.di.AppScope
import mendfield.shared.di.inMemoryStorageModule
import mendfield.shared.di.mendfieldModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

/**
 * The organizer's main path through the real app (navigation, ViewModels,
 * repositories, outbox) on the desktop, with a fake server: register an
 * item while offline, give it the slot triage suggests, log the outcome,
 * then reconnect and watch the outbox reach the server.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class OrganizerFlowUiTest {
    private val server = FakeCafeServer()
    private val connectivity = ManualConnectivity(initial = true)

    @BeforeTest fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val fakeNetwork = module {
            single<CafeRemote> { server }
            single<SyncTransport> { server }
            single<ConnectivityMonitor> { connectivity }
        }
        val koin = startKoin {
            modules(mendfieldModules(
                storage = inMemoryStorageModule,
                network = fakeNetwork,
                clock = TestClock(SampleData.at(-120)), // before the café opens
            ))
        }.koin
        koin.get<SyncCoordinator>().start(koin.get<AppScope>())
    }

    @AfterTest fun tearDown() {
        stopKoin() // cancels AppScope (onClose)
        Dispatchers.resetMain()
    }

    private fun ComposeUiTest.waitForText(text: String) = try {
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    } catch (e: Throwable) {
        println(onRoot().printToString())
        throw e
    }

    @Test fun intakeThenSlotThenOutcome() = runComposeUiTest {
        setContent { App() }
        waitForText("November Repair Café")
        onNodeWithText("November Repair Café").performClick()

        // Intake, offline: the save does not wait for the network.
        waitForText("Register item")
        connectivity.workOffline(true)
        server.online = false
        onNodeWithText("Register item").performClick()
        onNodeWithText("Audio").performClick()
        onNodeWithTag("description").performTextInput("Speaker crackles")
        onNodeWithTag("visitor").performTextInput("Lin Okoro")
        waitForText("Maud Okafor: best fit, 1 booked") // triage hint
        onNodeWithText("Save item").performClick()

        // Back on the board: select the waiting item, take the suggestion.
        waitForText("Audio · Speaker crackles")
        onNodeWithText("Audio · Speaker crackles").performClick()
        waitForText("Maud Okafor at 10:30 (best fit, 1 booked)")
        onNodeWithText("Maud Okafor at 10:30 (best fit, 1 booked)").performClick()
        onNodeWithTag("board").performScrollToNode(hasText("Speaker crackles (Lin Okoro)"))
        waitForText("Speaker crackles (Lin Okoro)")
        waitForText("Working offline · 2 changes waiting")

        // The outcome, from the slot.
        onNodeWithText("Speaker crackles (Lin Okoro)").performClick()
        waitForText("Maud Okafor, 10:30–11:00")
        onNodeWithText("Fixed").performClick()
        onNodeWithTag("note").performTextInput("Resoldered the jack")
        onNodeWithText("Save outcome").performClick()
        waitForText("Working offline · 3 changes waiting")

        onNodeWithText("Stats").performClick()
        waitForText("100 % repaired")
        onNodeWithText("Back").performClick()

        // Online again: the outbox drains in order, the bar says so.
        server.online = true
        connectivity.workOffline(false)
        waitForText("All changes sent")
        val data = server.data.getValue("evt-02")
        val item = data.items.single { it.description == "Speaker crackles" }
        assertEquals(item.id, data.slots.single { it.id == "evt-02.fx-01.2" }.itemId)
        assertEquals(OutcomeKind.FIXED, data.outcomes.single { it.itemId == item.id }.kind)
    }
}
