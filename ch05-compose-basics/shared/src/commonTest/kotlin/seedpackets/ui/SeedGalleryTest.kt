package seedpackets.ui

import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import seedpackets.model.SampleCatalog
import seedpackets.ui.theme.SeedTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SeedGalleryTest {
    private val packets = SampleCatalog.packets

    @Test
    fun favoriteToggleFlipsItsHoistedState() = runComposeUiTest {
        setContent { SeedTheme { SeedGallery(packets) } }

        val add = "Add Ember Cherry to favorites"
        onNodeWithContentDescription(add).performClick()

        // The icon and its description now read the new state.
        onNodeWithContentDescription("Remove Ember Cherry from favorites")
            .assertExists()
        onNodeWithContentDescription(add).assertDoesNotExist()
    }

    @Test
    fun stepperStopsAtTheStockAndUpdatesTheTotal() = runComposeUiTest {
        setContent { SeedTheme { SeedGallery(packets) } }
        val more = hasContentDescription("More packets") and
            hasAnyAncestorTag("card-tomato-ember")

        repeat(5) { onNode(more).performClick() }    // only 4 in stock

        onNode(more).assertIsNotEnabled()
        onNodeWithTag("total").assertTextEquals("4 packets reserved")
    }

    @Test
    fun soldOutPacketShowsNoStepper() = runComposeUiTest {
        setContent { SeedTheme { SeedGallery(packets) } }
        onNodeWithTag("gallery").performScrollToIndex(2)

        onNodeWithText("Sold out").assertExists()
        val more = hasContentDescription("More packets") and
            hasAnyAncestorTag("card-squash-moon")
        onNode(more).assertDoesNotExist()
    }

    @Test
    fun backToTopAppearsOnlyAfterScrolling() = runComposeUiTest {
        // A short window, so that the list really has to scroll.
        setContent {
            SeedTheme { SeedGallery(packets, Modifier.height(320.dp)) }
        }
        onNodeWithText("Back to top").assertDoesNotExist()

        onNodeWithTag("gallery").performScrollToIndex(4)
        onNodeWithText("Back to top").assertExists().performClick()

        onNodeWithText("Back to top").assertDoesNotExist()
    }

    private fun hasAnyAncestorTag(tag: String) = hasAnyAncestor(hasTestTag(tag))
}
