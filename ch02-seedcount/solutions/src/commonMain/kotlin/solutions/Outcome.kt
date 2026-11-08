package solutions

import seedcount.PlanResult
import seedcount.PlantingRow
import seedcount.Planner
import seedcount.SeedStock
import seedcount.message

// Exercise 2.4: a generic result type, and a text-to-plan pipeline.
sealed interface Outcome<out E, out T> {
    data class Success<out T>(val value: T) : Outcome<Nothing, T>
    data class Failure<out E>(val error: E) : Outcome<E, Nothing>
}

inline fun <E, T, R> Outcome<E, T>.map(transform: (T) -> R): Outcome<E, R> =
    when (this) {
        is Outcome.Success -> Outcome.Success(transform(value))
        is Outcome.Failure -> this
    }

inline fun <E, F, T> Outcome<E, T>.mapError(
    transform: (E) -> F,
): Outcome<F, T> =
    when (this) {
        is Outcome.Success -> this
        is Outcome.Failure -> Outcome.Failure(transform(error))
    }

inline fun <E, T, R> Outcome<E, T>.flatMap(
    transform: (T) -> Outcome<E, R>,
): Outcome<E, R> = when (this) {
    is Outcome.Success -> transform(value)
    is Outcome.Failure -> this
}

fun StockLine.toOutcome(): Outcome<StockLineError, SeedStock> = when (this) {
    is StockLine.Parsed -> Outcome.Success(stock)
    is StockLine.Invalid -> Outcome.Failure(error)
}

data class LineError(val lineNumber: Int, val error: StockLineError)

fun parseInventory(text: String): Outcome<List<LineError>, List<SeedStock>> {
    val parsed = text.lines()
        .withIndex()
        .filter { (_, line) -> line.isNotBlank() }
        .filterNot { (_, line) -> line.trim().startsWith("#") }
        .map { (index, line) -> index + 1 to parseStockLine(line).toOutcome() }
    val errors = parsed.mapNotNull { (number, outcome) ->
        (outcome as? Outcome.Failure)?.let { LineError(number, it.error) }
    }
    if (errors.isNotEmpty()) return Outcome.Failure(errors)
    return Outcome.Success(
        parsed.map { (_, outcome) -> (outcome as Outcome.Success).value },
    )
}

fun planFromText(
    inventory: String,
    rows: List<PlantingRow>,
): Outcome<List<String>, PlanResult.Ready> =
    parseInventory(inventory)
        .mapError { errors ->
            errors.map { "line ${it.lineNumber}: ${it.error}" }
        }
        .flatMap { stock ->
            when (val result = Planner(stock).plan(rows)) {
                is PlanResult.Ready -> Outcome.Success(result)
                is PlanResult.Rejected ->
                    Outcome.Failure(result.errors.map { it.message() })
            }
        }
