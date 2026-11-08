package hearthseed.labels.solutions

import androidx.lifecycle.ViewModel
import hearthseed.labels.LabelPrinter
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// Exercise 8.2: a ViewModel that needs a runtime value (the lot id).
class LabelDetailViewModel(
    val lotId: String,
    private val printer: LabelPrinter,
) : ViewModel() {
    val label: String = printer.labelFor(lotId) ?: "Unknown lot $lotId"
}

val detailModule = module {
    // lotId is not in the graph: parametersOf(...) supplies it.
    viewModelOf(::LabelDetailViewModel)
}
