// ch15-swift/Sources/SwiftBasics/Protocols.swift

/// A protocol plays the role of a Kotlin interface.
public protocol CatalogItem {
    var id: String { get }
    var title: String { get }
}

/// A protocol extension supplies default implementations.
extension CatalogItem {
    public var searchKey: String { title.lowercased() }
}

/// Conformance can be added later, in an extension.
extension Variety: CatalogItem {
    public var title: String { "\(commonName) (\(latinName))" }
}

extension SeedLot: CatalogItem {
    public var title: String { "Lot \(id), harvest \(harvestYear)" }
}

/// A generic function with a constraint, like `fun <T : CatalogItem>`.
public func search<Item: CatalogItem>(
    _ items: [Item], for text: String
) -> [Item] {
    let needle = text.lowercased()
    return items.filter { $0.searchKey.contains(needle) }
}

/// `any CatalogItem` is a box that can hold any conforming type.
public func titles(of items: [any CatalogItem]) -> [String] {
    items.map(\.title)
}

/// A generic type, like the server's `Page<T>`.
public struct Page<Item> {
    public var items: [Item]
    public var page: Int
    public var hasMore: Bool

    public init(items: [Item], page: Int, hasMore: Bool) {
        self.items = items
        self.page = page
        self.hasMore = hasMore
    }
}

/// Conditional conformance: a page is Sendable if its items are.
extension Page: Sendable where Item: Sendable {}

/// An extension on a constrained generic type: an "extension property"
/// that exists only on arrays of lots.
extension Array where Element == SeedLot {
    public var availableGrams: Int {
        filter(\.status.isAvailable).reduce(0) { $0 + $1.grams }
    }
}
