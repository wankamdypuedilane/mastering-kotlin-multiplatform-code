import SwiftUI
import Shared

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            VStack(spacing: 16) {
                // Kotlin top-level function from Greeting.kt, called from Swift.
                Text(GreetingKt.greeting(name: "SwiftUI"))
                ComposeView().frame(height: 120)
            }
        }
    }
}

/// Hosts the Compose Multiplatform UI inside SwiftUI.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
