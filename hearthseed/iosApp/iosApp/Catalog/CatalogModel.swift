// hearthseed/iosApp/iosApp/Catalog/CatalogModel.swift
import Observation
@preconcurrency import Shared // Kotlin headers carry no Sendable annotations

/// The SwiftUI catalog's state, kept current by the shared Kotlin ViewModel.
@MainActor
@Observable
final class CatalogModel {
    private(set) var snapshot: CatalogSnapshot?
    var searchText = ""
    var family = "" // "" = every family (a Picker tag cannot be nil)

    @ObservationIgnored private let observer: CatalogObserver
    @ObservationIgnored private var subscription: FlowSubscription?

    init(observer: CatalogObserver) {
        self.observer = observer
    }

    var varieties: [Variety] { snapshot?.varieties ?? [] }
    var phase: CatalogPhase { snapshot?.phase ?? .loading }

    /// Called from onAppear; stop() is called from onDisappear.
    func start() {
        guard subscription == nil else { return }
        subscription = observer.observe { [weak self] snapshot in
            // The bridge collects on Dispatchers.Main, the main thread.
            MainActor.assumeIsolated { self?.snapshot = snapshot }
        }
    }

    func stop() {
        subscription?.close()
        subscription = nil
    }

    func search() { observer.search(query: searchText) }

    func selectFamily() {
        do {
            try observer.selectFamily(family: family.isEmpty ? nil : family)
        } catch { // Kotlin @Throws(IllegalArgumentException::class)
            assertionFailure("\(error)") // the picker offers known ones
        }
    }

    func loadMore() { observer.loadMore() }
    func retry() { observer.retry() }

    func refresh() async {
        do {
            try await observer.refresh() // a Kotlin suspend function
        } catch {
            // Cancelled: the snapshot keeps the previous items.
        }
    }

    // Swift 6: the deinit of a @MainActor class is nonisolated and may run on
    // any thread. close() only cancels coroutines, which is thread-safe.
    deinit { observer.close() } // clears the ViewModel and its coroutines
}
