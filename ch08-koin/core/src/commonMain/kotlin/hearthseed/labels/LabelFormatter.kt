package hearthseed.labels

/** Turns a packet into the text printed on its label. */
fun interface LabelFormatter {
    fun format(packet: SeedPacket): String
}

/** The library's standard label: one line, fields separated by bars. */
class StandardLabelFormatter(private val libraryName: String) : LabelFormatter {
    override fun format(packet: SeedPacket): String = with(packet) {
        "$libraryName | $variety | $harvestYear | $grams g | lot $lotId"
    }
}
