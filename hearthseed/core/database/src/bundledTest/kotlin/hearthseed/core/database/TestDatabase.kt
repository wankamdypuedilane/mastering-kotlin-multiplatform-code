// hearthseed/core/database/src/bundledTest/kotlin/hearthseed/core/database/TestDatabase.kt
package hearthseed.core.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import hearthseed.core.model.Difficulty
import hearthseed.core.model.LotStatus
import hearthseed.core.model.SeedLot
import hearthseed.core.model.Variety

/** A fresh database in memory: fast, isolated, gone when closed. */
fun inMemoryDatabase(): HearthseedDatabase =
    Room.inMemoryDatabaseBuilder<HearthseedDatabase>()
        .setDriver(BundledSQLiteDriver()) // the same SQLite as the app
        .build()

fun variety(
    id: String, name: String, family: String, months: Set<Int> = setOf(4),
) =
    Variety(id, name, "Latin $name", family, 60, months, Difficulty.EASY, "")

fun lot(id: String, varietyId: String, grams: Int = 10,
        status: LotStatus = LotStatus.AVAILABLE) =
    SeedLot(id, varietyId, 2025, grams, 90, null, status)
