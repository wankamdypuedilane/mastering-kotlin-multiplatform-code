package hearthseed.labels.ui

import androidx.lifecycle.ViewModel
import hearthseed.labels.LabelPrinter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LabelUiState(
    val host: String,
    val labels: List<String> = emptyList(),
)

/** Screen logic only: it receives the printer, it never builds one. */
class LabelViewModel(
    private val printer: LabelPrinter,
    host: PrinterHost,
) : ViewModel() {
    private val _state = MutableStateFlow(LabelUiState(host = host.name))
    val state: StateFlow<LabelUiState> = _state.asStateFlow()

    fun printAll() {
        _state.update { it.copy(labels = printer.allLabels()) }
    }
}
