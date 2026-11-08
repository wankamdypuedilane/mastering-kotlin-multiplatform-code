// ch15-swift/Sources/SeedViews/CatalogView.swift
#if canImport(SwiftUI)
import SwiftUI
import SwiftBasics

/// The root of the catalog: it owns the model, so it holds it in @State.
public struct CatalogScreen: View {
    @State private var model = CatalogModel(service: InMemoryCatalog())

    public init() {}

    public var body: some View {
        CatalogView(model: model)
    }
}

/// A list with search and navigation. @Bindable lets it make bindings
/// ($model.searchText) to a model that someone else owns.
struct CatalogView: View {
    @Bindable var model: CatalogModel

    var body: some View {
        NavigationStack {
            List(model.visible) { variety in
                NavigationLink(value: variety) {
                    VarietyRow(variety: variety)
                }
            }
            .navigationTitle("Catalog")
            .navigationDestination(for: Variety.self) { variety in
                VarietyDetailView(variety: variety)
            }
            .searchable(text: $model.searchText)
            .overlay {
                if model.state == .loading { ProgressView() }
            }
            .task { await model.load() }   // cancelled if the view goes
        }
    }
}

struct VarietyRow: View {
    let variety: Variety

    var body: some View {
        VStack(alignment: .leading) {
            Text(variety.commonName).font(.headline)
            Text(variety.latinName).italic().foregroundStyle(.secondary)
        }
    }
}
#endif
