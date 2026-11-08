// ch15-swift/Tests/SwiftBasicsTests/LanguageTests.swift
import Testing
@testable import SwiftBasics

struct LanguageTests {
    let lot = Samples.lots[0]          // L1: 12 g, available, donor m1

    @Test func packetsRoundUp() {
        #expect(packetsNeeded(for: 12) == 3)
        #expect(packetsNeeded(for: 10) == 2)
        #expect(packetsNeeded(for: 7, packetSize: 7) == 1)
    }

    @Test func headlineUsesInterpolation() {
        #expect(headline(Samples.varieties[0])
            == "Larkmoor Red (Solanum lycopersicum), 11 weeks")
    }

    @Test func optionalsUnwrap() {
        let members = ["m1": Member(id: "m1", displayName: "Ada")]
        #expect(donorName(of: lot, members: members) == "Ada")
        #expect(donorName(of: lot, members: [:]) == "Former member m1")
        #expect(donorName(of: Samples.lots[2], members: members)
            == "Library stock")
        #expect(thankYouNote(for: Samples.lots[2]) == nil)
    }

    @Test func structsAreCopied() {
        var copy = lot
        copy.take(grams: 5)
        #expect(copy.grams == 7)
        #expect(lot.grams == 12)       // the original did not change
    }

    @Test func classesAreShared() {
        let shelf = Shelf(label: "A")
        let sameShelf = shelf
        sameShelf.store(lot)
        #expect(shelf.lots.count == 1)
        #expect(shelf === sameShelf)   // identity, like Kotlin's ===
    }

    @Test(arguments: [
        (LotStatus.available, "Available"),
        (.onLoan(memberId: "m2", dueSeason: 2025),
         "Overdue: m2 owes it since 2025"),
        (.onLoan(memberId: "m2", dueSeason: 2027), "On loan until 2027"),
        (.retired(reason: "too old"), "Retired (too old)"),
    ])
    func statusLabels(status: LotStatus, expected: String) {
        #expect(status.label(season: 2026) == expected)
    }

    @Test func protocolsAndGenerics() {
        #expect(search(Samples.varieties, for: "LACTUCA").map(\.id) == ["v3"])
        let mixed: [any CatalogItem] = [Samples.varieties[1], lot]
        #expect(titles(of: mixed)
            == ["Golden Runner (Phaseolus coccineus)", "Lot L1, harvest 2025"])
        #expect(Samples.lots.availableGrams == 42)   // 12 + 30
    }

    @Test func closures() {
        #expect(sowable(in: 3, from: Samples.varieties)
            == ["Winter Gem", "Larkmoor Red"])
        let label = makeLabelMaker(prefix: "LM")
        _ = label(lot)
        #expect(label(lot) == "LM-2: Lot L1, harvest 2025")
        #expect(Samples.lots.sorted(by: byHarvestYear).last?.id == "L2")

        let desk = ReturnDesk()
        var returned: [String] = []
        desk.onReturn { returned.append($0.id) }
        desk.receive(lot)
        #expect(returned == ["L1"])
    }

    @Test func typedThrows() throws {
        let loaned = try lend(lot, to: "m3", dueSeason: 2027)
        #expect(loaned.status == .onLoan(memberId: "m3", dueSeason: 2027))
        #expect(lot.status == .available)

        #expect(throws: LoanError.notAvailable(lotId: "L1")) {
            try lend(loaned, to: "m4", dueSeason: 2027)
        }
        #expect(loanMessage(Samples.lots[1], memberId: "m3")
            == "Lot L2 is out")
        #expect(loanMessage(lot, memberId: "m3") == "Lent L1 to m3")
        var tiny = lot
        tiny.grams = 3
        #expect(loanMessage(tiny, memberId: "m3") == "Only 3 g left")
        #expect(tryLend(loaned, to: "m4") == nil)
        #expect(lendResult(tiny, to: "m4")
            == .failure(.tooLittleSeed(grams: 3)))
    }
}
