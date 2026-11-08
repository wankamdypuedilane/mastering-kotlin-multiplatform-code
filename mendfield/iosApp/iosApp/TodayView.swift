// mendfield/iosApp/iosApp/TodayView.swift
import SwiftUI
import Shared

/// Holds the latest summary from Kotlin's TodayFeed. The feed calls back on
/// the main thread, so the property is only ever written there.
@Observable
final class TodayModel {
    var summary: TodaySummary?
    @ObservationIgnored private let feed = MainViewControllerKt.todayFeed()
    @ObservationIgnored private var subscription: FeedSubscription?

    func start() {
        guard subscription == nil else { return }
        subscription = feed.watch { [weak self] summary in
            self?.summary = summary
        }
    }

    func stop() {
        subscription?.cancel()
        subscription = nil
    }

    func refresh() {
        feed.refresh()
    }
}

/// A native SwiftUI screen over shared Kotlin data: the next event at a glance.
struct TodayView: View {
    @State private var model = TodayModel()

    var body: some View {
        NavigationStack {
            Group {
                if let summary = model.summary {
                    List {
                        Section {
                            Text(summary.eventTitle).font(.headline)
                            Text(summary.whenText).foregroundStyle(.secondary)
                        }
                        Section("Items") {
                            LabeledContent("Registered", value: "\(summary.registered)")
                            LabeledContent("Waiting for a slot", value: "\(summary.waiting)")
                            LabeledContent("Fixed", value: "\(summary.fixed)")
                            LabeledContent("Repair rate", value: summary.repairRate)
                        }
                        Section("Sync") {
                            LabeledContent("Server", value: summary.online ? "Reachable" : "Offline")
                            LabeledContent("Changes waiting", value: "\(summary.pendingChanges)")
                        }
                    }
                    .refreshable { model.refresh() }
                } else {
                    List {
                        ContentUnavailableView("No events yet",
                            systemImage: "calendar",
                            description: Text("Pull to refresh when the café server is reachable."))
                    }
                    .refreshable { model.refresh() }
                }
            }
            .navigationTitle("Today")
        }
        .onAppear { model.start() }
        .onDisappear { model.stop() }
    }
}
