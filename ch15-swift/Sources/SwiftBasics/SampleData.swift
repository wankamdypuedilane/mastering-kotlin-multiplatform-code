// ch15-swift/Sources/SwiftBasics/SampleData.swift

/// A small fictional dataset for tests and SwiftUI screens.
public enum Samples {
    public static let varieties: [Variety] = [
        Variety(id: "v1", commonName: "Larkmoor Red", latinName:
            "Solanum lycopersicum", family: "Solanaceae",
            daysToMaturity: 75, sowingMonths: [3, 4], difficulty: .medium),
        Variety(id: "v2", commonName: "Golden Runner", latinName:
            "Phaseolus coccineus", family: "Fabaceae",
            daysToMaturity: 90, sowingMonths: [5, 6], difficulty: .easy),
        Variety(id: "v3", commonName: "Winter Gem", latinName:
            "Lactuca sativa", family: "Asteraceae",
            daysToMaturity: 55, sowingMonths: [3, 8, 9], difficulty: .easy),
    ]

    public static let lots: [SeedLot] = [
        SeedLot(id: "L1", varietyId: "v1", harvestYear: 2025, grams: 12,
                germinationRate: 91, donorMemberId: "m1"),
        SeedLot(id: "L2", varietyId: "v1", harvestYear: 2024, grams: 4,
                germinationRate: 78,
                status: .onLoan(memberId: "m2", dueSeason: 2026)),
        SeedLot(id: "L3", varietyId: "v2", harvestYear: 2025, grams: 30,
                germinationRate: 88),
    ]
}

/// A service that answers from memory, optionally after a delay.
public struct InMemoryCatalog: VarietyService, LotService {
    public var delay: Duration

    public init(delay: Duration = .zero) {
        self.delay = delay
    }

    public func varieties() async throws -> [Variety] {
        try await Task.sleep(for: delay)
        return Samples.varieties
    }

    public func lots(forVariety varietyId: String) async throws -> [SeedLot] {
        try await Task.sleep(for: delay)
        return Samples.lots.filter { $0.varietyId == varietyId }
    }
}
