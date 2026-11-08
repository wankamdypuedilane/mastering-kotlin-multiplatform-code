package labels.app

import labels.core.LabelDesign
import labels.core.LabelSize
import labels.core.SampleVarieties
import labels.core.familyColor
import java.awt.datatransfer.DataFlavor
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import androidx.compose.ui.graphics.toArgb
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LabelExportTest {
    private val bean = SampleVarieties.all.first { it.id == "var-009" }

    private fun decode(png: ByteArray): BufferedImage =
        ImageIO.read(ByteArrayInputStream(png))

    @Test
    fun exportIsAPngOfThePrintSize() {
        val png = renderLabelPng(LabelDesign.forVariety(bean))

        val image = decode(png)
        assertEquals(827, image.width)   // 70 mm at 300 dpi
        assertEquals(472, image.height)  // 40 mm at 300 dpi
    }

    @Test
    fun borderStripeAndPaperHaveTheLabelColors() {
        val design = LabelDesign.forVariety(bean, LabelSize.LARGE)
        val image = decode(renderLabelPng(design))
        val density = design.size.exportDensity(EXPORT_DPI)
        val middle = image.height / 2

        assertEquals(LabelColors.ink.toArgb(), image.getRGB(1, middle))
        // stripe: 2 dp border + 2 dp padding, then 10 dp wide
        val stripeX = ((4 + 5) * density).toInt()
        val stripe = familyColor(bean.family).toInt()
        assertEquals(stripe, image.getRGB(stripeX, middle))
        val paperX = ((4 + 10 + 4) * density).toInt()
        val paperY = (3 * density).toInt()
        assertEquals(LabelColors.paper.toArgb(), image.getRGB(paperX, paperY))
    }

    @Test
    fun pngSelectionOffersAnImage() {
        val png = renderLabelPng(LabelDesign.forVariety(bean))
        val selection = PngSelection(png)

        assertTrue(selection.isDataFlavorSupported(DataFlavor.imageFlavor))
        val image = selection.getTransferData(DataFlavor.imageFlavor)
            as BufferedImage
        assertEquals(827, image.width)
    }
}
