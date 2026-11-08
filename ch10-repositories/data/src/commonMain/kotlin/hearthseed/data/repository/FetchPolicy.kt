package hearthseed.data.repository

/** How a one-shot read balances the cache against the network. */
enum class FetchPolicy {
    /** A fresh cache entry wins; the network is asked only when it is stale. */
    CACHE_FIRST,

    /** Always ask the network; fall back to the cache if the call fails. */
    NETWORK_FIRST,
}
