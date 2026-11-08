// ch04-germination-timer/timer/src/commonMain/.../GerminationReading.kt
package hearthseed.germination

/** One reading from a tray camera: seeds sown and seeds sprouted so far. */
data class GerminationReading(
    val trayId: String,
    val sown: Int,
    val sprouted: Int,
) {
    init {
        require(sown > 0) { "sown must be positive" }
        require(sprouted in 0..sown) { "sprouted must be in 0..sown" }
    }

    /** Germination rate in whole percent, rounded down. */
    val ratePercent: Int get() = sprouted * 100 / sown
}
