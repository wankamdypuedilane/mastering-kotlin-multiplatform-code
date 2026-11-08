// hearthseed/iosApp/iosApp/iOSApp.swift
import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Start Koin once, before the first screen asks for a ViewModel.
        MainViewControllerKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            // For now the whole UI is Compose; Chapter 16 adds SwiftUI screens.
            ComposeView().ignoresSafeArea(.keyboard)
        }
    }
}

/// Hosts the shared Compose UI (all screens and navigation) inside SwiftUI.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
