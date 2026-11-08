// hearthseed/iosApp/iosApp/iOSApp.swift
import SwiftUI
@preconcurrency import Shared

@main
struct iOSApp: App {
    init() {
        // Start Koin once, before the first screen asks for a ViewModel.
        MainViewControllerKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            TabView {
                // Option B (Chapter 16): native SwiftUI over shared ViewModels.
                CatalogScreen()
                    .tabItem { Label("Catalog", systemImage: "leaf") }
                MyLoansScreen() // Exercise 16.3
                    .tabItem { Label("My loans", systemImage: "tray") }
                // Option A: the whole Compose UI, unchanged.
                ComposeView()
                    .ignoresSafeArea(.keyboard)
                    .tabItem { Label("Compose", systemImage: "square.stack") }
            }
        }
    }
}

/// Hosts the shared Compose UI (all screens and navigation) inside SwiftUI.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController,
                                context: Context) {}
}
