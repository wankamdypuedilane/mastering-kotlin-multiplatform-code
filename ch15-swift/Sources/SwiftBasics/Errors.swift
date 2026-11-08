// ch15-swift/Sources/SwiftBasics/Errors.swift

/// Errors are values of a type that conforms to `Error`.
public enum LoanError: Error, Equatable {
    case notAvailable(lotId: String)
    case tooLittleSeed(grams: Int)
}

/// Typed throws: this function can throw only a LoanError.
public func lend(
    _ lot: SeedLot, to memberId: String, dueSeason: Int
) throws(LoanError) -> SeedLot {
    guard lot.status.isAvailable else {
        throw .notAvailable(lotId: lot.id)
    }
    guard lot.grams >= 5 else {
        throw .tooLittleSeed(grams: lot.grams)
    }
    var loaned = lot                      // a copy: `lot` is unchanged
    loaned.status = .onLoan(memberId: memberId, dueSeason: dueSeason)
    return loaned
}

/// Every call that can throw is marked with `try`.
public func loanMessage(_ lot: SeedLot, memberId: String) -> String {
    do {
        let loaned = try lend(lot, to: memberId, dueSeason: 2027)
        return "Lent \(loaned.id) to \(memberId)"
    } catch {
        // With typed throws, `error` is a LoanError, not `any Error`.
        switch error {
        case .notAvailable(let lotId): return "Lot \(lotId) is out"
        case .tooLittleSeed(let grams): return "Only \(grams) g left"
        }
    }
}

/// `try?` turns an error into nil; Result stores either outcome.
public func tryLend(_ lot: SeedLot, to memberId: String) -> SeedLot? {
    try? lend(lot, to: memberId, dueSeason: 2027)
}

public func lendResult(
    _ lot: SeedLot, to memberId: String
) -> Result<SeedLot, LoanError> {
    Result { () throws(LoanError) in
        try lend(lot, to: memberId, dueSeason: 2027)
    }
}
