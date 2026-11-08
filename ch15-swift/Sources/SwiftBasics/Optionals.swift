// ch15-swift/Sources/SwiftBasics/Optionals.swift

public struct Member: Identifiable, Hashable, Sendable {
    public let id: String
    public var displayName: String

    public init(id: String, displayName: String) {
        self.id = id
        self.displayName = displayName
    }
}

/// `guard let` unwraps or leaves early, the shape of Kotlin's `?: return`.
public func donorName(of lot: SeedLot, members: [String: Member]) -> String {
    guard let donorId = lot.donorMemberId else {
        return "Library stock"
    }
    // A dictionary lookup returns an optional; `?.` and `??` as in Kotlin.
    return members[donorId]?.displayName ?? "Former member \(donorId)"
}

/// `if let x` (shorthand for `if let x = x`) unwraps inside the block.
public func thankYouNote(for lot: SeedLot) -> String? {
    if let donorMemberId = lot.donorMemberId {
        return "Thank you, \(donorMemberId), for lot \(lot.id)!"
    }
    return nil
}
