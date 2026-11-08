// ch15-swift/Tests/SolutionsTests/SolutionsTests.swift
import Testing
import SwiftBasics
@testable import Solutions

/// Counts calls; answers after a short delay so that callers overlap.
actor CountingService: LotService {
    private(set) var calls = 0

    func lots(forVariety varietyId: String) async throws -> [SeedLot] {
        calls += 1
        try await Task.sleep(for: .milliseconds(50))
        return Samples.lots.filter { $0.varietyId == varietyId }
    }
}

struct SolutionsTests {
    @Test func germinationTest() {
        var test = GerminationTest(lotId: "L1", sown: 50)
        #expect(test.rate == nil)
        test.record(sprouted: 43)
        #expect(test.result == .counted(sown: 50, sprouted: 43))
        #expect(test.rate == 86)
        #expect(test.applied(to: Samples.lots[0]).germinationRate == 86)
        test.record(sprouted: 10)               // already counted: ignored
        #expect(test.rate == 86)
        test.discard(reason: "mould")
        #expect(test.rate == nil)
        #expect(test.applied(to: Samples.lots[0]).germinationRate == 91)
    }

    @Test func cacheLoadsOnceForConcurrentCallers() async throws {
        let service = CountingService()
        let cache = LotCache(service: service)
        async let first = cache.lots(forVariety: "v1")
        async let second = cache.lots(forVariety: "v1")
        let (a, b) = try await (first, second)
        #expect(a == b && a.count == 2)
        _ = try await cache.lots(forVariety: "v1")      // cached
        #expect(await service.calls == 1)
        await cache.invalidate(varietyId: "v1")
        _ = try await cache.lots(forVariety: "v1")
        #expect(await service.calls == 2)
    }

    @Test @MainActor func lotListModel() async {
        let cache = LotCache(service: CountingService())
        let model = LotListModel(varietyId: "v1", season: 2026, cache: cache)
        await model.load()
        #expect(model.state == .loaded)
        #expect(model.visibleLots.map(\.id) == ["L1", "L2"])
        model.onlyAvailable = true
        #expect(model.visibleLots.map(\.id) == ["L1"])
    }
}
