package seedswap.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class) // currentTime
class InMemorySwapRepositoryTest {
    private val basil = Proposal("o4", "Lena Moss", "Genovese basil", 10, "")

    @Test
    fun boardStartsWithTheSampleOffers() = runTest {
        val repository = InMemorySwapRepository()
        assertEquals(5, repository.offers().first().size)
        assertEquals("Golden zucchini", repository.offer("o3").first()?.variety)
        assertEquals(null, repository.offer("nope").first())
    }

    @Test
    fun proposingCountsOnTheOfferAndTakesTheLatency() = runTest {
        val repository = InMemorySwapRepository(latency = 2.seconds)
        val result = repository.propose(basil)

        assertEquals(ProposalResult.Sent("p1"), result)
        assertEquals(2_000, currentTime)
        assertEquals(1, repository.offer("o4").first()?.proposals)
        assertEquals(listOf(basil), repository.proposals.value)
    }

    @Test
    fun proposingToAnUnknownOfferFails() = runTest {
        val repository = InMemorySwapRepository()
        val result = repository.propose(basil.copy(offerId = "gone"))
        assertEquals(ProposalResult.OfferNotFound, result)
        assertEquals(emptyList(), repository.proposals.value)
    }
}
