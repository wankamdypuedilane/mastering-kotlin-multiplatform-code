package labels.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LabelDesignTest {
    private val ember = SampleVarieties.all.first { it.id == "var-001" }

    @Test
    fun sowingWindowGroupsConsecutiveMonths() {
        assertEquals("Feb–Mar, Oct–Nov", sowingWindow(setOf(11, 2, 10, 3)))
        assertEquals("May", sowingWindow(setOf(5)))
        assertEquals("Mar–Sep", sowingWindow((3..9).toSet()))
        assertEquals("any time", sowingWindow(emptySet()))
    }

    @Test
    fun defaultTextAndFileName() {
        val design = LabelDesign.forVariety(ember)
        assertEquals("Sow Mar–Apr · ready in 72 days", design.sowingText)
        assertEquals("larkmoor-ember-tomato-70x40.png", design.fileName)
        assertNull(design.problem)
    }

    @Test
    fun blankOrLongTextCannotBeExported() {
        val design = LabelDesign.forVariety(ember)
        val blank = design.copy(sowingText = " ")
        assertEquals("Sowing text is empty", blank.problem)
        val long = design.copy(sowingText = "x".repeat(MAX_SOWING_TEXT + 1))
        assertTrue(long.problem!!.startsWith("Sowing text is longer"))
    }

    @Test
    fun exportPixelsAt300Dpi() {
        assertEquals(827 to 472, LabelSize.STANDARD.exportPixels(300))
        assertEquals(1181 to 709, LabelSize.LARGE.exportPixels(300))
        // 70 mm x 4 dp/mm = 280 dp; 280 dp x density = 827 px
        val density = LabelSize.STANDARD.exportDensity(300)
        assertEquals(827f, 280 * density, absoluteTolerance = 0.5f)
    }

    @Test
    fun familyColorsHaveAFallback() {
        assertEquals(0xFF2E7D32, familyColor("Fabaceae"))
        assertEquals(0xFF757575, familyColor("Rosaceae"))
    }
}
