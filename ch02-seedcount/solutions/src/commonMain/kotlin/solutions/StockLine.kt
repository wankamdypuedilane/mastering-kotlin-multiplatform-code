package solutions

import seedcount.GerminationRate
import seedcount.SeedStock

// Exercise 2.3: parse "Winter Kale;3;100;90" into a SeedStock.
sealed interface StockLineError {
    data class WrongFieldCount(val found: Int) : StockLineError
    data class NotANumber(val field: String, val text: String) : StockLineError
    data class OutOfRange(val field: String, val value: Int) : StockLineError
}

sealed interface StockLine {
    data class Parsed(val stock: SeedStock) : StockLine
    data class Invalid(val error: StockLineError) : StockLine
}

fun parseStockLine(line: String): StockLine {
    val fields = line.split(";").map { it.trim() }
    if (fields.size != 4) {
        return StockLine.Invalid(StockLineError.WrongFieldCount(fields.size))
    }
    val (name, packetsText, seedsText, rateText) = fields
    val numbers = listOf(
        "packets" to packetsText,
        "seedsPerPacket" to seedsText,
        "rate" to rateText,
    ).map { (field, text) ->
        text.toIntOrNull()
            ?: return StockLine.Invalid(StockLineError.NotANumber(field, text))
    }
    val (packets, seeds, percent) = numbers
    val error = when {
        packets < 0 -> StockLineError.OutOfRange("packets", packets)
        seeds < 1 -> StockLineError.OutOfRange("seedsPerPacket", seeds)
        else -> null
    }
    if (error != null) return StockLine.Invalid(error)
    val rate = GerminationRate.of(percent)
        ?: return StockLine.Invalid(StockLineError.OutOfRange("rate", percent))
    return StockLine.Parsed(SeedStock(name, packets, seeds, rate))
}
