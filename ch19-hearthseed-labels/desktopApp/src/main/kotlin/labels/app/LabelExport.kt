package labels.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.Density
import labels.core.LabelDesign
import org.jetbrains.skia.EncodedImageFormat
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

const val EXPORT_DPI = 300

/** Renders [design] off screen and encodes it as PNG bytes. */
fun renderLabelPng(design: LabelDesign, dpi: Int = EXPORT_DPI): ByteArray {
    val (width, height) = design.size.exportPixels(dpi)
    val density = Density(design.size.exportDensity(dpi))
    val image = renderComposeScene(width, height, density) {
        SeedLabel(design, Modifier.fillMaxSize())
    }
    val data = checkNotNull(image.encodeToData(EncodedImageFormat.PNG)) {
        "PNG encoding failed"
    }
    return data.bytes
}

/** Offers a PNG to other applications as an image (AWT data transfer). */
class PngSelection(private val png: ByteArray) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> =
        arrayOf(DataFlavor.imageFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
        flavor == DataFlavor.imageFlavor

    override fun getTransferData(flavor: DataFlavor): Any {
        if (!isDataFlavorSupported(flavor)) {
            throw UnsupportedFlavorException(flavor)
        }
        return ImageIO.read(ByteArrayInputStream(png))
    }
}
