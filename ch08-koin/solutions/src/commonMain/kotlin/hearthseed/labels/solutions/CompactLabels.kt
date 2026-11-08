package hearthseed.labels.solutions

import hearthseed.labels.LabelFormatter
import hearthseed.labels.SeedPacket
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Exercise 8.1: a second LabelFormatter, told apart by a qualifier.
class CompactLabelFormatter : LabelFormatter {
    override fun format(packet: SeedPacket): String =
        "${packet.lotId} ${packet.variety} (${packet.harvestYear})"
}

val Compact = named("compact")

val compactModule = module {
    single<LabelFormatter>(Compact) { CompactLabelFormatter() }
}
