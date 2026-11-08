// ch15-swift/Sources/SwiftBasics/ValueTypes.swift

/// A struct is a value type: assigning it copies it.
public struct SeedLot: Identifiable, Hashable, Sendable {
    public let id: String
    public let varietyId: String
    public var harvestYear: Int
    public var grams: Int
    public var germinationRate: Int        // percent
    public var donorMemberId: String?      // optional, like String?
    public var status: LotStatus

    public init(
        id: String, varietyId: String, harvestYear: Int, grams: Int,
        germinationRate: Int, donorMemberId: String? = nil,
        status: LotStatus = .available
    ) {
        self.id = id
        self.varietyId = varietyId
        self.harvestYear = harvestYear
        self.grams = grams
        self.germinationRate = germinationRate
        self.donorMemberId = donorMemberId
        self.status = status
    }

    /// A method that changes a struct must be marked `mutating`.
    public mutating func take(grams amount: Int) {
        grams -= min(amount, grams)
    }
}

/// A class is a reference type: every variable shares one instance.
public final class Shelf {
    public let label: String
    public private(set) var lots: [SeedLot] = []

    public init(label: String) {
        self.label = label
    }

    public func store(_ lot: SeedLot) {
        lots.append(lot)          // no `mutating`: the reference is shared
    }
}
