// ch08-koin/core/src/commonMain/kotlin/hearthseed/labels/LocatorPrinter.kt
package hearthseed.labels

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Service locator: the class fetches its own dependencies (avoid). */
class LocatorPrinter : KoinComponent {
    private val repository: PacketRepository by inject()
    private val formatter: LabelFormatter by inject()

    fun allLabels(): List<String> = repository.all().map(formatter::format)
}
