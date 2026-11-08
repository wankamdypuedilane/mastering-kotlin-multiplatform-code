// ch04-germination-timer/timer/src/commonMain/.../SproutTally.kt
package hearthseed.germination

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Sprouts reported by many sensors at once. */
class SproutTally {
    var total = 0
        private set

    /** WRONG: another coroutine can run at the suspension point. */
    suspend fun addUnsafe(sprouts: Int, confirm: suspend () -> Unit) {
        val before = total
        confirm()                 // suspends: others read the same value
        total = before + sprouts
    }

    /** Right: the whole read-suspend-write holds the lock. */
    private val mutex = Mutex()
    suspend fun add(sprouts: Int, confirm: suspend () -> Unit) =
        mutex.withLock {          // one coroutine at a time; waiting suspends
            val before = total
            confirm()
            total = before + sprouts
        }

    private val _perTray = MutableStateFlow(mapOf<String, Int>())
    val perTray: StateFlow<Map<String, Int>> = _perTray.asStateFlow()

    /** No suspension inside: update retries until its change applies. */
    fun record(trayId: String, sprouts: Int) = _perTray.update { counts ->
        counts + (trayId to (counts[trayId] ?: 0) + sprouts)
    }
}
