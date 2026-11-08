// hearthseed/iosApp/iosApp/Catalog/CatalogView.swift
import SwiftUI
@preconcurrency import Shared

/// The native catalog: SwiftUI views over the shared CatalogViewModel.
struct CatalogScreen: View {
    // The initial value of a @State property is evaluated again every time
    // SwiftUI re-creates this struct: create the model once, in onAppear.
    @State private var model: CatalogModel? // created once, in onAppear

    var body: some View {
        NavigationStack {
            Group {
                if let model { CatalogList(model: model) }
            }
            .navigationTitle("Catalog")
            .navigationDestination(for: String.self) { varietyId in
                VarietyDetailScreen(varietyId: varietyId)
            }
        }
        .onAppear {
            if model == nil {
                model = CatalogModel(observer: ObserversKt.catalogObserver())
            }
        }
    }
}

struct CatalogList: View {
    @Bindable var model: CatalogModel

    var body: some View {
        List {
            FamilyPicker(model: model)
            ForEach(model.varieties, id: \.id) { variety in
                NavigationLink(value: variety.id) {
                    VarietyRow(variety: variety)
                }
            }
            if model.snapshot?.canLoadMore == true {
                ProgressView()
                    .onAppear { model.loadMore() } // the end is visible
            }
            if let footer = model.snapshot?.countLabel(), !footer.isEmpty {
                Text(footer).foregroundStyle(.secondary) // Exercise 16.2
            }
        }
        .overlay { CatalogOverlay(model: model) }
        .searchable(text: $model.searchText)
        .onChange(of: model.searchText) { model.search() }
        .refreshable { await model.refresh() }
        .onAppear { model.start() }
        .onDisappear { model.stop() }
    }
}

/// Loading, empty and error states over the (empty) list.
struct CatalogOverlay: View {
    let model: CatalogModel

    var body: some View {
        switch model.phase {
        case .loading:
            ProgressView()
        case .empty:
            ContentUnavailableView.search(text: model.searchText)
        case .failed:
            ContentUnavailableView {
                Label("Catalog unavailable", systemImage: "wifi.slash")
            } description: {
                Text(model.snapshot?.message ?? "")
            } actions: {
                Button("Retry") { model.retry() }
            }
        default: // a Kotlin enum is a class in Swift: no exhaustive switch
            EmptyView()
        }
    }
}

struct FamilyPicker: View {
    @Bindable var model: CatalogModel

    var body: some View {
        Picker("Family", selection: $model.family) {
            Text("All families").tag("")
            ForEach(model.snapshot?.families ?? [], id: \.self) { family in
                Text(family).tag(family)
            }
        }
        .onChange(of: model.family) { model.selectFamily() }
    }
}

struct VarietyRow: View {
    let variety: Variety

    var body: some View {
        VStack(alignment: .leading) {
            Text(variety.commonName).font(.headline)
            Text("\(variety.latinName) · \(variety.daysToMaturity) days")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
    }
}
