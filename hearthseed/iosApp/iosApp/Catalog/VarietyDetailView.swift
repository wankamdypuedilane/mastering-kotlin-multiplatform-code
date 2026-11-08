// hearthseed/iosApp/iosApp/Catalog/VarietyDetailView.swift
import SwiftUI
@preconcurrency import Shared

struct VarietyDetailScreen: View {
    let varietyId: String
    @State private var model: VarietyDetailModel?

    var body: some View {
        Group {
            if let model { VarietyDetailContent(model: model) }
        }
        .onAppear {
            if model == nil {
                model = VarietyDetailModel(observer:
                    ObserversKt.varietyDetailObserver(varietyId: varietyId))
            }
        }
    }
}

struct VarietyDetailContent: View {
    let model: VarietyDetailModel

    var body: some View {
        List {
            if let variety = model.snapshot?.variety {
                Section {
                    LabeledContent("Latin name", value: variety.latinName)
                    LabeledContent("Family", value: variety.family)
                    LabeledContent("Days to maturity",
                                   value: "\(variety.daysToMaturity)")
                    LabeledContent("Sow in months", value: sowingText(variety))
                    // `description` is NSObject's: Kotlin renames the property.
                    Text(variety.description_)
                }
            }
            Section("Seed lots") {
                ForEach(model.snapshot?.lots ?? [], id: \.id) { lot in
                    LabeledContent("Harvest \(lot.harvestYear)",
                                   value: lot.isBorrowable ? "available"
                                                           : "unavailable")
                }
            }
            if let message = model.snapshot?.message {
                Section {
                    Text(message)
                    Button("Retry") { model.retry() }
                }
            }
        }
        .overlay {
            if model.snapshot?.isLoading ?? true { ProgressView() }
        }
        .navigationTitle(model.snapshot?.variety?.commonName ?? "")
        .onAppear { model.start() }
        .onDisappear { model.stop() }
    }
}
