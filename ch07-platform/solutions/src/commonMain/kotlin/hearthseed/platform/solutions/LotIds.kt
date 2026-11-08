package hearthseed.platform.solutions

import hearthseed.platform.IdGenerator
import kotlin.uuid.Uuid

/** Exercise 7.2: ids such as "lot-6f1c...". */
class PrefixedIds(private val prefix: String) : IdGenerator {
    override fun newId(): String = "$prefix-${Uuid.random()}"

    /** The UUID inside [id], or null if [id] is not one of ours. */
    fun parse(id: String): Uuid? =
        id.removePrefix("$prefix-")
            .takeIf { it.length < id.length }
            ?.let(Uuid::parseHexDashOrNull)
}
