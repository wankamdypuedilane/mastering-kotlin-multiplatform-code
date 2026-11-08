// ch15-swift/Sources/Solutions/Exercise3View.swift
#if canImport(SwiftUI)
import SwiftUI
import SwiftBasics

public struct LotListView: View {
    @Bindable var model: LotListModel

    public init(model: LotListModel) {
        self.model = model
    }

    public var body: some View {
        List {
            Toggle("Only available lots", isOn: $model.onlyAvailable)
            switch model.state {
            case .failed(let message):
                Text(message).foregroundStyle(.red)
            default:
                ForEach(model.visibleLots) { lot in
                    LotRow(lot: lot, season: model.season)
                }
            }
        }
        .navigationTitle("Lots")
        .task { await model.load() }
    }
}

struct LotRow: View {
    let lot: SeedLot
    let season: Int

    var body: some View {
        VStack(alignment: .leading) {
            Text(lot.title).font(.headline)
            Text("\(lot.grams) g, \(lot.germinationRate) % germination")
            Text(lot.status.label(season: season))
                .foregroundStyle(
                    lot.status.isAvailable ? Color.green : .secondary)
        }
    }
}
#endif
