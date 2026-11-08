package hearthseed.labels

/**
 * Manual dependency injection: one composition root builds the whole graph.
 * Every class receives its dependencies through its constructor.
 */
class ManualContainer(
    libraryName: String = "Larkmoor Commons",
    repository: PacketRepository = DemoPacketRepository(),
) {
    private val formatter: LabelFormatter = StandardLabelFormatter(libraryName)
    val printer: LabelPrinter = LabelPrinter(repository, formatter)

    // A new sheet on every call: the caller chooses the column count.
    fun sheet(columns: Int): LabelSheet = LabelSheet(columns, printer)
}
