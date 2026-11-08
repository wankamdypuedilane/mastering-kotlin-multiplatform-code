package hearthseed.platform

import kotlin.uuid.Uuid

/** Produces unique identifiers; tests replace it with a predictable one. */
fun interface IdGenerator {
    fun newId(): String
}

/** Random (version 4) UUIDs from the standard library, on every target. */
object UuidIdGenerator : IdGenerator {
    override fun newId(): String = Uuid.random().toString()
}
