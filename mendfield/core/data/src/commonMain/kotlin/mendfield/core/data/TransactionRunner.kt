// mendfield/core/data/src/commonMain/kotlin/mendfield/core/data/TransactionRunner.kt
package mendfield.core.data

/**
 * Runs a block of storage writes as one unit. Room implements it with one
 * SQLite write transaction (RoomTransactionRunner in :core:database), so the
 * outbox and the cache change together or not at all.
 */
interface TransactionRunner {
    suspend fun <R> inTransaction(block: suspend () -> R): R
}

/**
 * For stores that cannot share a transaction, such as the web's memory cache
 * and localStorage outbox: the writes simply run in order.
 */
object SequentialWrites : TransactionRunner {
    override suspend fun <R> inTransaction(block: suspend () -> R): R = block()
}
