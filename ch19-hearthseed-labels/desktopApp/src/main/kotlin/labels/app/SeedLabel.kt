package labels.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import labels.core.LabelDesign
import labels.core.familyColor

/** Fixed print colors: a label must look the same in light and dark mode. */
object LabelColors {
    val paper = Color(0xFFFBF6E9)
    val ink = Color(0xFF1F3B2D)
}

/** The printable label. Foundation only: no theme needed to render it. */
@Composable
fun SeedLabel(design: LabelDesign, modifier: Modifier = Modifier) {
    val v = design.variety
    Row(
        modifier
            .background(LabelColors.paper)
            .border(2.dp, LabelColors.ink),
    ) {
        Box(Modifier.padding(2.dp).width(10.dp).fillMaxHeight()
            .background(Color(familyColor(v.family))))
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            BasicText(v.commonName, style = TextStyle(LabelColors.ink,
                fontSize = 17.sp, fontWeight = FontWeight.Bold))
            if (design.showLatinName) {
                BasicText(v.latinName, style = TextStyle(LabelColors.ink,
                    fontSize = 12.sp, fontStyle = FontStyle.Italic))
            }
            Spacer(Modifier.height(8.dp))
            BasicText(design.sowingText,
                style = TextStyle(LabelColors.ink, fontSize = 12.sp))
            Spacer(Modifier.weight(1f))
            BasicText("Larkmoor Commons Seed Library · ${v.id}",
                style = TextStyle(LabelColors.ink, fontSize = 9.sp))
        }
    }
}

/** The label at its preview size (4 dp per millimeter). */
@Composable
fun SeedLabelPreview(design: LabelDesign, modifier: Modifier = Modifier) {
    val mm = labels.core.LabelSize.PREVIEW_DP_PER_MM
    Box(modifier.width((design.size.widthMm * mm).dp)
        .height((design.size.heightMm * mm).dp)) {
        SeedLabel(design, Modifier.fillMaxSize())
    }
}
