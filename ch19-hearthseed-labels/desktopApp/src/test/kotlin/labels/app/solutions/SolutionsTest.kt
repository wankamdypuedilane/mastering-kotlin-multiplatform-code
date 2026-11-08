package labels.app.solutions

import labels.core.LabelDesign
import labels.core.SampleVarieties
import labels.core.familyColor
import androidx.compose.ui.ExperimentalComposeUiApi
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SolutionsTest {
    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun clipEntryCarriesPlainText() {
        val entry = sowingTextClipEntry("Sow thinly")
        val transferable = entry.nativeClipEntry as Transferable
        val text = transferable.getTransferData(DataFlavor.stringFlavor)
        assertEquals("Sow thinly", text)
    }

    @Test
    fun sheetHasOneCellPerLabelInAGrid() {
        val designs = SampleVarieties.all.take(5)
            .map { LabelDesign.forVariety(it) }
        val png = renderSheetPng(designs, columns = 2)

        val image = ImageIO.read(ByteArrayInputStream(png))
        assertEquals(2 * 827, image.width)    // 2 columns
        assertEquals(3 * 472, image.height)   // 5 labels -> 3 rows
        val density = designs.first().size.exportDensity(300)
        val stripe = ((4 + 5) * density).toInt()
        designs.forEachIndexed { i, design ->
            val x = (i % 2) * 827 + stripe
            val y = (i / 2) * 472 + 236
            val stripeColor = familyColor(design.variety.family).toInt()
            assertEquals(stripeColor, image.getRGB(x, y))
        }
        assertEquals(0xFFFFFFFF.toInt(), image.getRGB(827 + 400, 2 * 472 + 236))
    }

    @Test
    fun sheetRejectsMixedSizes() {
        val small = LabelDesign.forVariety(SampleVarieties.all[0])
        val large = small.copy(size = labels.core.LabelSize.LARGE)
        assertFailsWith<IllegalArgumentException> {
            renderSheetPng(listOf(small, large), columns = 2)
        }
    }
}
