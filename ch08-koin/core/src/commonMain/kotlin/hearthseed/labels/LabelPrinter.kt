package hearthseed.labels

/** The packet label printer service: it needs a repository and a formatter. */
class LabelPrinter(
    private val repository: PacketRepository,
    private val formatter: LabelFormatter,
) {
    fun labelFor(lotId: String): String? =
        repository.byLot(lotId)?.let(formatter::format)

    fun allLabels(): List<String> = repository.all().map(formatter::format)
}

/** Labels laid out in [columns]; the column count is a runtime value. */
class LabelSheet(val columns: Int, private val printer: LabelPrinter) {
    init { require(columns > 0) { "columns must be positive" } }

    fun rows(): List<List<String>> = printer.allLabels().chunked(columns)
}
