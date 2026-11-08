// mendfield/solutions/src/commonMain/kotlin/mendfield/solutions/FreeCapacity.kt
package mendfield.solutions

import kotlin.time.Instant
import mendfield.core.data.EventBoard

/** Exercise 23.1: how many open slots each fixer still has. */
data class FixerCapacity(val fixerId: String, val name: String, val free: Int)

/**
 * Open slots that start at or after [notBefore], per fixer of the board,
 * most free first, then by name. Fixers without slots are left out.
 */
fun EventBoard.freeCapacity(notBefore: Instant): List<FixerCapacity> {
    val bySlotOwner = slots.groupBy { it.fixerId }
    return fixers
        .filter { it.id in bySlotOwner }
        .map { fixer ->
            val free = bySlotOwner.getValue(fixer.id)
                .count { it.isOpen && it.start >= notBefore }
            FixerCapacity(fixer.id, fixer.displayName, free)
        }
        .sortedWith(compareByDescending<FixerCapacity> { it.free }
            .thenBy { it.name })
}
