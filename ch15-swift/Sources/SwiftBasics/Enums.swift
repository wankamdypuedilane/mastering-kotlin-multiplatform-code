// ch15-swift/Sources/SwiftBasics/Enums.swift

/// Each case can carry its own data: Swift's answer to a sealed class.
public enum LotStatus: Hashable, Sendable {
    case available
    case onLoan(memberId: String, dueSeason: Int)
    case retired(reason: String)
}

extension LotStatus {
    /// `switch` must be exhaustive, like `when` over a sealed type.
    public func label(season: Int) -> String {
        switch self {
        case .available:
            "Available"
        case let .onLoan(memberId, dueSeason) where dueSeason < season:
            "Overdue: \(memberId) owes it since \(dueSeason)"
        case .onLoan(_, let dueSeason):
            "On loan until \(dueSeason)"
        case .retired(let reason):
            "Retired (\(reason))"
        }
    }

    /// `if case` matches one pattern without a full switch.
    public var isAvailable: Bool {
        if case .available = self { true } else { false }
    }
}
