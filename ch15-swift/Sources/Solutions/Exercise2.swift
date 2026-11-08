// ch15-swift/Sources/Solutions/Exercise2.swift
import SwiftBasics

/// Exercise 15.2: loads each variety's lots once, even when several
/// callers ask at the same moment.
public actor LotCache {
    private let service: any LotService
    private var cached: [String: [SeedLot]] = [:]
    private var inFlight: [String: Task<[SeedLot], any Error>] = [:]

    public init(service: any LotService) {
        self.service = service
    }

    public func lots(forVariety id: String) async throws -> [SeedLot] {
        if let hit = cached[id] { return hit }
        // Actors are reentrant: while one caller awaits the network,
        // another may enter. It must join the running load.
        if let running = inFlight[id] { return try await running.value }

        let task = Task { try await service.lots(forVariety: id) }
        inFlight[id] = task
        defer { inFlight[id] = nil }
        let lots = try await task.value
        cached[id] = lots
        return lots
    }

    public func invalidate(varietyId id: String) {
        cached[id] = nil
    }
}
