// hearthseed/iosApp/iosApp/Catalog/VarietyDetailModel.swift
import Observation
@preconcurrency import Shared

/// One variety and its lots, from the shared VarietyDetailViewModel.
@MainActor
@Observable
final class VarietyDetailModel {
    private(set) var snapshot: VarietyDetailSnapshot?

    @ObservationIgnored private let observer: VarietyDetailObserver
    @ObservationIgnored private var subscription: FlowSubscription?

    init(observer: VarietyDetailObserver) {
        self.observer = observer
    }

    func start() {
        guard subscription == nil else { return }
        subscription = observer.observe { [weak self] snapshot in
            MainActor.assumeIsolated { self?.snapshot = snapshot }
        }
    }

    func stop() {
        subscription?.close()
        subscription = nil
    }

    func retry() { observer.retry() }

    deinit { observer.close() }
}

/// Kotlin types seen from Swift: Int is Int32, a Set<Int> holds KotlinInt.
func sowingText(_ variety: Variety) -> String {
    monthsText(variety.sowingMonths.map { Int($0.intValue) })
}
