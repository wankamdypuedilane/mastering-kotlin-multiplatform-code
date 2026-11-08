// ch15-swift/Sources/SwiftBasics/Concurrency.swift

/// The data source, abstracted. Sendable: safe to share across tasks.
public protocol LotService: Sendable {
    func lots(forVariety varietyId: String) async throws -> [SeedLot]
}

/// `async let` starts two child tasks; `await` collects both.
public func lotCount(
    _ first: String, _ second: String, service: some LotService
) async throws -> Int {
    async let a = service.lots(forVariety: first)
    async let b = service.lots(forVariety: second)
    return try await a.count + b.count
}

/// A task group runs one child per variety, like many `async` in a
/// Kotlin `coroutineScope`. The group waits for all of them.
public func availableGrams(
    for varietyIds: [String], service: some LotService
) async throws -> [String: Int] {
    try await withThrowingTaskGroup(of: (String, Int).self) { group in
        for id in varietyIds {
            group.addTask {
                let lots = try await service.lots(forVariety: id)
                return (id, lots.availableGrams)
            }
        }
        var grams: [String: Int] = [:]
        for try await (id, total) in group {
            grams[id] = total
        }
        return grams
    }
}

/// An actor protects its state: one caller at a time inside.
public actor LoanLedger {
    private var borrower: [String: String] = [:]   // lotId -> memberId

    public init() {}

    /// Returns false if someone else already has the lot.
    public func borrow(lotId: String, memberId: String) -> Bool {
        guard borrower[lotId] == nil else { return false }
        borrower[lotId] = memberId
        return true
    }

    public func memberHolding(lotId: String) -> String? {
        borrower[lotId]
    }
}

/// Checks for cancellation between slow steps.
public func checkAll(
    _ lots: [SeedLot], step: Duration = .milliseconds(10)
) async throws -> Int {
    var checked = 0
    for _ in lots {
        try Task.checkCancellation()   // throws CancellationError
        try await Task.sleep(for: step)
        checked += 1
    }
    return checked
}
