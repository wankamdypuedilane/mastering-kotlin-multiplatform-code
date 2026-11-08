// hearthseed/shared/src/jvmTest/kotlin/hearthseed/shared/LiveServerUiTest.kt
package hearthseed.shared

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import hearthseed.core.network.ApiConfig
import hearthseed.core.network.HearthseedApi
import hearthseed.core.network.createHttpClient
import hearthseed.shared.di.AppConfig
import hearthseed.shared.di.hearthseedModules
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

/**
 * The whole app against a real, freshly started hearthseed-server:
 *   HEARTHSEED_E2E_URL=http://localhost:8085/api/v1/ ./gradlew :shared:jvmTest
 * Without the variable the test does nothing, so CI stays offline.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class LiveServerUiTest {
    private val baseUrl: String? = System.getenv("HEARTHSEED_E2E_URL")

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    private fun ComposeUiTest.waitForText(text: String) {
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
        println("shown: $text")
    }

    @Test fun browseBorrowAndLoseARace() {
        val url = baseUrl ?: return println("no HEARTHSEED_E2E_URL: skipped")
        startKoin { modules(hearthseedModules(AppConfig(baseUrl = url))) }
        runComposeUiTest {
            setContent { App() }
            waitForText("Larkmoor Ember tomato")
            val list = onNodeWithTag("varieties")
            list.performScrollToNode(hasText("Load more (20 of 40)"))
            onNodeWithText("Load more (20 of 40)").performClick()
            list.performScrollToNode(hasText("All shown (40)"))
            waitForText("All shown (40)")

            onNodeWithText("Search varieties").performTextInput("pea")
            waitForText("All shown (1)")
            onNodeWithText("Larkmoor Pea").performClick()
            waitForText("2023 harvest, 8 g, 88% germination")

            onAllNodesWithText("Borrow").onFirst().performClick() // lot-013
            onNodeWithText("Borrow this lot").performClick()
            waitForText("See my loans")
            onNodeWithText("See my loans").performClick()
            waitForText("Lot lot-013")
            onNodeWithText("Back").performClick()
            waitForText("On loan")

            // Another member takes lot-014 behind the app's back.
            runBlocking {
                val client = createHttpClient(ApiConfig(baseUrl = url))
                val other = HearthseedApi(client, "demo-token-ada")
                println("other member: " + other.borrow("lot-014", "mem-001"))
                client.close()
            }
            onNodeWithText("Borrow").performClick() // the stale lot-014 row
            onNodeWithText("Borrow this lot").performClick()
            waitForText("Another member borrowed this lot a moment ago.")
            onNodeWithText("Back to the variety").performClick()
            // The 409 reloaded the lots: nothing is left to borrow.
            waitUntil(timeoutMillis = 10_000) {
                onAllNodesWithText("On loan").fetchSemanticsNodes().size == 2
            }
            println("lots now: 2 x On loan, no Borrow button: " +
                onAllNodesWithText("Borrow").fetchSemanticsNodes().isEmpty())
        }
    }
}
