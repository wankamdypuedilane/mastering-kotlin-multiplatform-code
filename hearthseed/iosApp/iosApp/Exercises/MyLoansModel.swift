// hearthseed/iosApp/iosApp/Exercises/MyLoansModel.swift
import Observation
@preconcurrency import Shared

/// Exercise 16.3: the member's loans, from the shared MyLoansViewModel.
@MainActor
@Observable
final class MyLoansModel {
    private(set) var snapshot: MyLoansSnapshot?

    @ObservationIgnored private let observer: MyLoansObserver
    @ObservationIgnored private var subscription: FlowSubscription?

    init(observer: MyLoansObserver) {
        self.observer = observer
    }

    var openLoans: [Loan] { snapshot?.open ?? [] }
    var returnedLoans: [Loan] { snapshot?.returned ?? [] }

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

    func refresh() { observer.refresh() }

    deinit { observer.close() }
}
