package labels.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import labels.core.LabelDesign
import labels.core.LabelSize
import labels.core.Variety
import labels.core.defaultSowingText

/** The state of one editor window, shared by its menus and its content. */
class EditorState(varieties: List<Variety>) {
    var varieties by mutableStateOf(varieties)
        private set
    var selected by mutableStateOf(varieties.first())
        private set
    var sowingText by mutableStateOf(defaultSowingText(varieties.first()))
    var size by mutableStateOf(LabelSize.STANDARD)
    var status by mutableStateOf("Ready")

    val design: LabelDesign
        get() = LabelDesign(selected, sowingText, size)

    fun select(variety: Variety) {
        selected = variety
        sowingText = defaultSowingText(variety)
    }

    fun selectNext(step: Int) {
        val i = varieties.indexOf(selected) + step
        if (i in varieties.indices) select(varieties[i])
    }

    fun load(list: List<Variety>) {
        if (list.isEmpty()) return
        varieties = list
        select(list.first())
    }
}
