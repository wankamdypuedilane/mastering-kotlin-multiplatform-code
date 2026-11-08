// mendfield/iosApp/iosApp/iOSApp.swift
import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Koin, Room and the sync loop, once, before the first screen.
        MainViewControllerKt.startMendfield()
    }

    var body: some Scene {
        WindowGroup {
            TabView {
                TodayView()
                    .tabItem { Label("Today", systemImage: "sun.max") }
                ComposeView()
                    .ignoresSafeArea(.keyboard)
                    .tabItem { Label("Organizer", systemImage: "wrench.and.screwdriver") }
            }
        }
    }
}

/// Hosts the shared Compose app (every organizer screen) inside SwiftUI.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
