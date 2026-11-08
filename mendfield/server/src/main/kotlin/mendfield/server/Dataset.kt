// mendfield/server/src/main/kotlin/mendfield/server/Dataset.kt
package mendfield.server

import kotlinx.serialization.Serializable

// The shape of src/main/resources/dataset.json. Slots are not listed:
// the store builds each fixer's grid with slotGrid() from :core:domain.

@Serializable
internal data class Dataset(
    val fixers: List<FixerDto>,
    val events: List<DatasetEvent>,
    val items: List<DatasetItem>,
    val outcomes: List<OutcomeDto>,
)

@Serializable
internal data class DatasetEvent(
    val event: EventDto,
    val slotMinutes: Int,
    val fixerIds: List<String>,
)

@Serializable
internal data class DatasetItem(val item: ItemDto, val slotId: String? = null)
