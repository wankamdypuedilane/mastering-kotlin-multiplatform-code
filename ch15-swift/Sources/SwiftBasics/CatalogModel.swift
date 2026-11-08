// ch15-swift/Sources/SwiftBasics/CatalogModel.swift
import Observation

public protocol VarietyService: Sendable {
    func varieties() async throws -> [Variety]
}

public enum LoadState: Equatable, Sendable {
    case idle, loading, loaded, failed(String)
}

/// The state of the catalog screen. @Observable tracks every stored
/// property; @MainActor keeps all access on the main thread.
@MainActor
@Observable
public final class CatalogModel {
    public private(set) var varieties: [Variety] = []
    public private(set) var state: LoadState = .idle
    public var searchText = ""

    private let service: any VarietyService

    public init(service: any VarietyService) {
        self.service = service
    }

    /// Derived state: recomputed when `varieties` or `searchText` change.
    public var visible: [Variety] {
        searchText.isEmpty ? varieties : search(varieties, for: searchText)
    }

    public func load() async {
        state = .loading
        do {
            varieties = try await service.varieties()  // suspends here
            state = .loaded                            // back on main
        } catch {
            state = .failed("Could not load the catalog")
        }
    }
}
