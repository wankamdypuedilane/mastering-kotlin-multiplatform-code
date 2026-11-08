// ch05-compose-basics/shared/src/commonTest/.../ui/AdaptivePacketGridTest.kt
package seedpackets.ui

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import seedpackets.model.SampleCatalog
import seedpackets.ui.theme.SeedTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AdaptivePacketGridTest {
    private val packets = SampleCatalog.packets.take(2)

    private fun ComposeUiTest.showGrid(width: Dp) = setContent {
        SeedTheme {
            AdaptivePacketGrid(packets, Modifier.requiredSize(width, 600.dp)) {
                SeedPacketCard(it, false, {}, 0, {})
            }
        }
    }

    private fun ComposeUiTest.bounds(id: String): DpRect =
        onNodeWithTag("card-$id").getUnclippedBoundsInRoot()

    @Test
    fun columnRuleHasOneBreakpoint() {
        assertEquals(1, columnsFor(599.dp))
        assertEquals(2, columnsFor(600.dp))
        assertEquals(3, columnsFor(900.dp))
    }

    @Test
    fun narrowSpaceStacksTheCards() = runComposeUiTest {
        showGrid(400.dp)
        val first = bounds("bean-runner")
        val second = bounds("tomato-ember")
        assertEquals(first.left, second.left)        // same column
        assertTrue(second.top > first.bottom)        // below the first
    }

    @Test
    fun wideSpacePutsTheCardsSideBySide() = runComposeUiTest {
        showGrid(900.dp)
        val first = bounds("bean-runner")
        val second = bounds("tomato-ember")
        assertEquals(first.top, second.top)          // same row
        assertTrue(second.left > first.right)        // to the right
    }
}
