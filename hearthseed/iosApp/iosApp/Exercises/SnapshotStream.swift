// hearthseed/iosApp/iosApp/Exercises/SnapshotStream.swift
@preconcurrency import Shared

/// Exercise 16.4: a Kotlin FlowWatcher as a Swift AsyncSequence.
extension AsyncStream where Element == CatalogSnapshot {
    init(_ watcher: FlowWatcher<CatalogSnapshot>) {
        self.init { continuation in
            let subscription = watcher.watch { snapshot in
                continuation.yield(snapshot)
            }
            // The consumer stopped (break, or its task was cancelled).
            continuation.onTermination = { _ in subscription.close() }
        }
    }
}

/// Waits for the first loaded page, then stops watching.
@MainActor
func firstLoadedPage(of observer: CatalogObserver) async -> CatalogSnapshot? {
    for await snapshot in AsyncStream(observer.snapshots())
    where snapshot.phase == .loaded {
        return snapshot // ends the stream: onTermination closes it
    }
    return nil
}
