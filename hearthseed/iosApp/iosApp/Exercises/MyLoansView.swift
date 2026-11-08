// hearthseed/iosApp/iosApp/Exercises/MyLoansView.swift
import SwiftUI
@preconcurrency import Shared

/// Exercise 16.3: "My loans" in SwiftUI.
struct MyLoansScreen: View {
    @State private var model: MyLoansModel?

    var body: some View {
        NavigationStack {
            Group {
                if let model { MyLoansList(model: model) }
            }
            .navigationTitle("My loans")
        }
        .onAppear {
            if model == nil {
                model = MyLoansModel(observer: ObserversKt.myLoansObserver())
            }
        }
    }
}

struct MyLoansList: View {
    let model: MyLoansModel

    var body: some View {
        List {
            Section("Open") {
                ForEach(model.openLoans, id: \.id) { loan in
                    LabeledContent(loan.lotId,
                                   value: "return in \(loan.dueSeason)")
                }
            }
            Section("Returned") {
                ForEach(model.returnedLoans, id: \.id) { loan in
                    Text(loan.lotId)
                }
            }
            if let message = model.snapshot?.message {
                Text(message).foregroundStyle(.secondary)
            }
        }
        .overlay {
            if model.snapshot?.isLoading ?? true { ProgressView() }
        }
        .refreshable { model.refresh() }
        .onAppear { model.start() }
        .onDisappear { model.stop() }
    }
}
