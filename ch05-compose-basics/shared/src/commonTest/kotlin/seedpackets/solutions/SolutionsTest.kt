package seedpackets.solutions

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import seedpackets.model.Family
import seedpackets.model.SampleCatalog
import seedpackets.ui.theme.SeedTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SolutionsTest {
    @Test
    fun familyTagReadsItsStringResource() = runComposeUiTest {
        setContent { SeedTheme { FamilyTag(Family.SQUASHES) } }
        onNodeWithText("Squashes").assertExists()
    }

    @Test
    fun familyChipFiltersTheGallery() = runComposeUiTest {
        setContent { SeedTheme { FilteredGallery(SampleCatalog.packets) } }
        onNodeWithTag("card-tomato-ember").assertExists()

        onNodeWithText("Legumes").performClick()

        onNodeWithText("Legumes").assertIsSelected()
        onNodeWithTag("card-bean-runner").assertExists()
        onNodeWithTag("card-pea-ladder").assertExists()
        onNodeWithTag("card-tomato-ember").assertDoesNotExist()

        onNodeWithText("All").performClick()
        onNodeWithTag("card-tomato-ember").assertExists()
    }

    @Test
    fun themeModeSaverRoundTrips() {
        val scope = SaverScope { true }
        for (mode in ThemeMode.entries) {
            val saved = with(ThemeModeSaver) { scope.save(mode) }
            assertEquals(mode, ThemeModeSaver.restore(saved!!))
        }
    }

    @Test
    fun themeModeSwitchesTheColorScheme() = runComposeUiTest {
        var surfaceIsDark: Boolean? = null
        setContent {
            ThemeModeHost {
                val surface = MaterialTheme.colorScheme.surface
                surfaceIsDark = surface.luminance() < 0.5f
            }
        }

        onNodeWithText("Dark").performClick()
        waitForIdle()
        assertTrue(surfaceIsDark == true)

        onNodeWithText("Light").performClick()
        waitForIdle()
        assertFalse(surfaceIsDark == true)
        onNodeWithText("Light").assertIsSelected()
    }
}
