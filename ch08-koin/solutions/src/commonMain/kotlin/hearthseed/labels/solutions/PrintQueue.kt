package hearthseed.labels.solutions

import hearthseed.labels.LabelPrinter
import hearthseed.labels.coreModule
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

// Exercise 8.3: the platform caller, not expect/actual, supplies the sink.
fun interface LabelSink {
    fun send(label: String)
}

class PrintQueue(
    private val printer: LabelPrinter,
    private val sink: LabelSink,
) {
    fun printAll(): Int = printer.allLabels().onEach(sink::send).size
}

// Each app loads its own sink module next to it (a Bluetooth printer, a file).
val queueModule = module {
    includes(coreModule)
    factoryOf(::PrintQueue)
}
