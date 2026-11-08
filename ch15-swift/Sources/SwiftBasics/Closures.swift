// ch15-swift/Sources/SwiftBasics/Closures.swift

/// Trailing closures and `$0`: the Swift spelling of Kotlin's `it`.
public func sowable(in month: Int, from varieties: [Variety]) -> [String] {
    varieties
        .filter { $0.sowingMonths.contains(month) }
        .sorted { $0.daysToMaturity < $1.daysToMaturity }
        .map(\.commonName)          // a key path used as a function
}

/// A closure with named parameters and an explicit type. A global
/// constant must be Sendable in Swift 6, hence `@Sendable`.
public let byHarvestYear: @Sendable (SeedLot, SeedLot) -> Bool = {
    first, second in
    first.harvestYear > second.harvestYear
}

/// The returned closure captures `printed` and keeps it alive.
public func makeLabelMaker(prefix: String) -> (SeedLot) -> String {
    var printed = 0
    return { lot in
        printed += 1
        return "\(prefix)-\(printed): \(lot.title)"
    }
}

/// A closure stored for later must be marked @escaping.
public final class ReturnDesk {
    private var handlers: [(SeedLot) -> Void] = []

    public init() {}

    public func onReturn(_ handler: @escaping (SeedLot) -> Void) {
        handlers.append(handler)
    }

    public func receive(_ lot: SeedLot) {
        handlers.forEach { $0(lot) }
    }
}
