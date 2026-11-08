package seedpackets.ui

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import seedpackets.ui.theme.SeedTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalTestApi::class)
class ModifierOrderTest {
    @Test
    fun sameModifiersInAnotherOrderPaintAnotherArea() = runComposeUiTest {
        setContent { SeedTheme(darkTheme = false) { ModifierOrderDemo() } }

        for (tag in listOf("padding-first", "background-first")) {
            onNodeWithTag(tag)
                .assertWidthIsEqualTo(80.dp)
                .assertHeightIsEqualTo(80.dp)
        }
        val a = onNodeWithTag("padding-first").captureToImage().toPixelMap()
        val b = onNodeWithTag("background-first").captureToImage().toPixelMap()
        val mid = a.width / 2

        // Both centers are painted; only the background-first corner is.
        assertEquals(a[mid, mid], b[mid, mid])
        assertEquals(b[mid, mid], b[2, 2])
        assertNotEquals(a[mid, mid], a[2, 2])
    }
}
