package labels.core

import kotlin.math.roundToInt

/** Printable label sizes. The preview draws [PREVIEW_DP_PER_MM] dp per mm. */
enum class LabelSize(val widthMm: Int, val heightMm: Int) {
    STANDARD(70, 40),
    LARGE(100, 60),
    ;

    /** Pixel size of an export at [dpi] dots per inch. */
    fun exportPixels(dpi: Int): Pair<Int, Int> =
        mmToPx(widthMm, dpi) to mmToPx(heightMm, dpi)

    /** Compose density that makes the preview layout fill the export. */
    fun exportDensity(dpi: Int): Float = dpi / MM_PER_INCH / PREVIEW_DP_PER_MM

    companion object {
        const val PREVIEW_DP_PER_MM = 4f
        private const val MM_PER_INCH = 25.4f
        fun mmToPx(mm: Int, dpi: Int): Int =
            (mm * dpi / MM_PER_INCH).roundToInt()
    }
}

const val MAX_SOWING_TEXT = 90

/** Everything a label shows. Pure data: easy to test, render or save. */
data class LabelDesign(
    val variety: Variety,
    val sowingText: String,
    val size: LabelSize = LabelSize.STANDARD,
    val showLatinName: Boolean = true,
) {
    /** Why the label cannot be exported, or null if it can. */
    val problem: String?
        get() = when {
            sowingText.isBlank() -> "Sowing text is empty"
            sowingText.length > MAX_SOWING_TEXT ->
                "Sowing text is longer than $MAX_SOWING_TEXT characters"
            else -> null
        }

    /** "larkmoor-ember-tomato-70x40.png" */
    val fileName: String
        get() = variety.commonName.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-").trim('-') +
            "-${size.widthMm}x${size.heightMm}.png"

    companion object {
        fun forVariety(variety: Variety, size: LabelSize = LabelSize.STANDARD) =
            LabelDesign(variety, defaultSowingText(variety), size)
    }
}

/** "Sow Mar–Apr · ready in 72 days" */
fun defaultSowingText(variety: Variety): String =
    "Sow ${sowingWindow(variety.sowingMonths)} · " +
        "ready in ${variety.daysToMaturity} days"
