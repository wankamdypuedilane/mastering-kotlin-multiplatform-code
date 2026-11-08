// ch15-swift/Sources/Solutions/Exercise3Model.swift
import Observation
import SwiftBasics

/// Exercise 15.3: the state behind the lot list screen.
@MainActor
@Observable
public final class LotListModel {
    public let varietyId: String
    public let season: Int
    public private(set) var lots: [SeedLot] = []
    public private(set) var state: LoadState = .idle
    public var onlyAvailable = false

    private let cache: LotCache

    public init(varietyId: String, season: Int, cache: LotCache) {
        self.varietyId = varietyId
        self.season = season
        self.cache = cache
    }

    /// Newest harvest first; optionally only lots on the shelf.
    public var visibleLots: [SeedLot] {
        let shown = onlyAvailable ? lots.filter(\.status.isAvailable) : lots
        return shown.sorted(by: byHarvestYear)
    }

    public func load() async {
        state = .loading
        do {
            lots = try await cache.lots(forVariety: varietyId)
            state = .loaded
        } catch {
            state = .failed("Could not load the lots")
        }
    }
}
