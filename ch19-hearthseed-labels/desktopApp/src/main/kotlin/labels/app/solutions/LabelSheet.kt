package labels.app.solutions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.renderComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import labels.app.EXPORT_DPI
import labels.app.SeedLabel
import labels.core.LabelDesign
import labels.core.LabelSize
import org.jetbrains.skia.EncodedImageFormat

/** Exercise 19.3: rows of [columns] labels of the same size on one PNG. */
fun renderSheetPng(
    designs: List<LabelDesign>,
    columns: Int,
    dpi: Int = EXPORT_DPI,
): ByteArray {
    require(designs.isNotEmpty() && columns > 0)
    val size = designs.first().size
    require(designs.all { it.size == size }) { "All labels must have one size" }
    val rows = (designs.size + columns - 1) / columns
    val (w, h) = size.exportPixels(dpi)
    val image = renderComposeScene(w * columns, h * rows,
        Density(size.exportDensity(dpi))) {
        LabelSheet(designs.chunked(columns), size)
    }
    return checkNotNull(image.encodeToData(EncodedImageFormat.PNG)).bytes
}

@Composable
private fun LabelSheet(rows: List<List<LabelDesign>>, size: LabelSize) {
    val mm = LabelSize.PREVIEW_DP_PER_MM
    val cell = Modifier.size((size.widthMm * mm).dp, (size.heightMm * mm).dp)
    Column(Modifier.background(Color.White)) {
        rows.forEach { row ->
            Row {
                row.forEach { SeedLabel(it, cell) }
                // An incomplete last row stays white.
                repeat(rows.first().size - row.size) { Spacer(cell) }
            }
        }
    }
}
