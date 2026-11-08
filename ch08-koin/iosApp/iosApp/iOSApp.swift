import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Start Koin once, before the first view asks for a dependency.
        KoinIosKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ComposeView().ignoresSafeArea(.keyboard)
        }
    }
}

/// Hosts the shared Compose screen, whose ViewModel comes from Koin.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ controller: UIViewController,
                                context: Context) {}
}
