package hearthseed.data.solutions

import hearthseed.data.domain.SowableVarieties
import hearthseed.data.model.Variety
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Exercise 10.2: sowable in [month], ready within [maxDays], fastest first. */
class QuickHarvest(private val sowable: SowableVarieties) {
    operator fun invoke(month: Int, maxDays: Int): Flow<List<Variety>> =
        sowable(month).map { list ->
            list.filter { it.daysToMaturity <= maxDays }
                .sortedBy { it.daysToMaturity }
        }
}
