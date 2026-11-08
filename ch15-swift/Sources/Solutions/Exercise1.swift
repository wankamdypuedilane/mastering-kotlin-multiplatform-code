// ch15-swift/Sources/Solutions/Exercise1.swift
import SwiftBasics

/// Exercise 15.1: the result of a germination test.
public enum GerminationResult: Equatable, Sendable {
    case pending(sown: Int)
    case counted(sown: Int, sprouted: Int)
    case discarded(reason: String)
}

public struct GerminationTest: Sendable {
    public let lotId: String
    public private(set) var result: GerminationResult

    public init(lotId: String, sown: Int) {
        self.lotId = lotId
        self.result = .pending(sown: sown)
    }

    /// The rate in percent, or nil until the tray has been counted.
    public var rate: Int? {
        guard case let .counted(sown, sprouted) = result, sown > 0 else {
            return nil
        }
        return sprouted * 100 / sown
    }

    /// Records the count. Does nothing unless the test is pending.
    public mutating func record(sprouted: Int) {
        guard case .pending(let sown) = result else { return }
        result = .counted(sown: sown, sprouted: min(sprouted, sown))
    }

    public mutating func discard(reason: String) {
        result = .discarded(reason: reason)
    }

    /// Returns a copy of the lot with the measured rate, if any.
    public func applied(to lot: SeedLot) -> SeedLot {
        guard let rate else { return lot }
        var updated = lot
        updated.germinationRate = rate
        return updated
    }
}
