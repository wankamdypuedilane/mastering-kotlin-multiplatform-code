// ch15-swift/Sources/SwiftBasics/Basics.swift

/// How hard a variety is to grow and to keep true to type.
public enum Difficulty: String, CaseIterable, Sendable {
    case easy, medium, hard
}

/// An open-pollinated variety in the Larkmoor catalog.
public struct Variety: Identifiable, Hashable, Sendable {
    public let id: String
    public var commonName: String
    public var latinName: String
    public var family: String
    public var daysToMaturity: Int
    public var sowingMonths: Set<Int>
    public var difficulty: Difficulty

    // Structs get a memberwise init for free, but it is internal:
    // other modules (our SwiftUI target) need this public one.
    public init(
        id: String, commonName: String, latinName: String,
        family: String, daysToMaturity: Int,
        sowingMonths: Set<Int>, difficulty: Difficulty = .medium
    ) {
        self.id = id
        self.commonName = commonName
        self.latinName = latinName
        self.family = family
        self.daysToMaturity = daysToMaturity
        self.sowingMonths = sowingMonths
        self.difficulty = difficulty
    }

    /// A computed property, like a Kotlin `val` with a getter.
    public var weeksToHarvest: Int { (daysToMaturity + 6) / 7 }
}

/// Argument labels: callers write `packetsNeeded(for: 12)`.
public func packetsNeeded(for grams: Int, packetSize: Int = 5) -> Int {
    let full = grams / packetSize       // let: like Kotlin val
    var packets = full                  // var: like Kotlin var
    if grams % packetSize != 0 {
        packets += 1
    }
    return packets
}

/// String interpolation uses \( ) instead of ${ }.
public func headline(_ variety: Variety) -> String {
    "\(variety.commonName) (\(variety.latinName)), "
        + "\(variety.weeksToHarvest) weeks"
}
