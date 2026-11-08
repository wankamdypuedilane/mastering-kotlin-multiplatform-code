// swift-tools-version: 6.2
// ch15-swift/Package.swift
import PackageDescription

let package = Package(
    name: "SwiftBasics",
    // Observation (@Observable) needs macOS 14 / iOS 17.
    platforms: [.macOS(.v14), .iOS(.v17)],
    products: [
        .library(name: "SwiftBasics", targets: ["SwiftBasics"]),
        .library(name: "SeedViews", targets: ["SeedViews"]),
    ],
    targets: [
        .target(name: "SwiftBasics"),
        // SwiftUI views: compiled only where SwiftUI exists (Apple platforms).
        .target(name: "SeedViews", dependencies: ["SwiftBasics"]),
        .target(name: "Solutions", dependencies: ["SwiftBasics"]),
        .testTarget(name: "SwiftBasicsTests", dependencies: ["SwiftBasics"]),
        .testTarget(
            name: "SolutionsTests",
            dependencies: ["Solutions", "SwiftBasics"]
        ),
    ]
)
