// ch08-koin/core/src/commonMain/kotlin/hearthseed/labels/PrintRun.kt
package hearthseed.labels

import org.koin.dsl.module

/** Marks one print run; Koin keeps one scope per run. */
class PrintRun                             // the scope's type

/** Numbers the labels of one run: #1, #2... State that lives with the run. */
class NumberedLabels(private val printer: LabelPrinter) {
    private var count = 0

    fun next(lotId: String): String? =
        printer.labelFor(lotId)?.let { "#${++count} $it" }
}

val printRunModule = module {
    scope<PrintRun> {
        scoped { NumberedLabels(get()) }   // one per scope, shared inside it
    }
}
