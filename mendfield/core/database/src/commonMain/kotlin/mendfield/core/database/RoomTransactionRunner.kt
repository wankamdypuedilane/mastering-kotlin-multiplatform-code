// mendfield/core/database/src/commonMain/kotlin/mendfield/core/database/RoomTransactionRunner.kt
package mendfield.core.database

import androidx.room3.withWriteTransaction
import mendfield.core.data.TransactionRunner

/**
 * One SQLite write transaction around the block. The DAO calls inside it
 * (RoomOutboxStore.add, then RoomCafeCache.apply) use the transaction's
 * connection, so an exception in either rolls both back.
 */
class RoomTransactionRunner(private val db: MendfieldDatabase) : TransactionRunner {
    override suspend fun <R> inTransaction(block: suspend () -> R): R =
        db.withWriteTransaction { block() }
}
