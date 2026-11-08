// ch15-swift/Tests/SwiftBasicsTests/ConcurrencyTests.swift
import Testing
@testable import SwiftBasics

struct ConcurrencyTests {
    let service = InMemoryCatalog()

    @Test func asyncLetAndTaskGroup() async throws {
        #expect(try await lotCount("v1", "v2", service: service) == 3)
        let grams = try await availableGrams(
            for: ["v1", "v2", "v3"], service: service)
        #expect(grams == ["v1": 12, "v2": 30, "v3": 0])
    }

    @Test func actorLetsOnlyOneBorrowerWin() async {
        let ledger = LoanLedger()
        let wins = await withTaskGroup(of: Bool.self) { group in
            for member in 1...100 {
                group.addTask {
                    await ledger.borrow(lotId: "L1", memberId: "m\(member)")
                }
            }
            var count = 0
            for await won in group where won { count += 1 }
            return count
        }
        #expect(wins == 1)
        #expect(await ledger.memberHolding(lotId: "L1") != nil)
    }

    @Test func cancellationStopsTheLoop() async {
        let lots = Array(repeating: Samples.lots[0], count: 1_000)
        let task = Task { try await checkAll(lots) }
        task.cancel()
        let result = await task.result
        #expect(throws: CancellationError.self) { try result.get() }
    }

    @Test @MainActor func modelLoadsAndFilters() async {
        let model = CatalogModel(service: service)
        #expect(model.state == .idle)
        await model.load()
        #expect(model.state == .loaded)
        #expect(model.visible.count == 3)
        model.searchText = "runner"
        #expect(model.visible.map(\.id) == ["v2"])
    }

    struct Broken: VarietyService {
        struct Offline: Error {}
        func varieties() async throws -> [Variety] { throw Offline() }
    }

    @Test @MainActor func modelReportsFailure() async {
        let model = CatalogModel(service: Broken())
        await model.load()
        #expect(model.state == .failed("Could not load the catalog"))
        #expect(model.varieties.isEmpty)
    }
}
