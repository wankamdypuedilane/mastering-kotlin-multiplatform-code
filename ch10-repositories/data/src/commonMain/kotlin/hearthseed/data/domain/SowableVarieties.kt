package hearthseed.data.domain

import hearthseed.data.model.Variety
import hearthseed.data.repository.VarietyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A use case: the varieties a member can sow in [month], easiest first.
 * It holds a rule that several screens share; it does no I/O of its own.
 */
class SowableVarieties(private val repository: VarietyRepository) {
    operator fun invoke(month: Int): Flow<List<Variety>> {
        require(month in 1..12) { "month must be 1..12, was $month" }
        return repository.observeVarieties().map { all ->
            all.filter { month in it.sowingMonths }
                .sortedWith(compareBy({ it.difficulty }, { it.commonName }))
        }
    }
}
