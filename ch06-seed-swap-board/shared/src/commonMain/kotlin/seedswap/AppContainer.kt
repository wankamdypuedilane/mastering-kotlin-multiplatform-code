package seedswap

import kotlin.time.Duration.Companion.milliseconds
import seedswap.core.InMemorySwapRepository
import seedswap.core.SwapRepository
import seedswap.session.MemberSession

/**
 * Manual dependency injection: one repository for the whole process,
 * created outside composition so it survives configuration changes.
 * Chapter 8 replaces this object with Koin.
 */
object AppContainer {
    val repository: SwapRepository by lazy {
        InMemorySwapRepository(latency = 600.milliseconds)
    }

    /** App-wide state, shared by every screen that needs it. */
    val session: MemberSession by lazy { MemberSession() }
}
