// ch15-swift/Sources/SeedViews/VarietyDetailView.swift
#if canImport(SwiftUI)
import SwiftUI
import SwiftBasics

/// Local, private UI state lives in @State; SwiftUI keeps it alive
/// while the view is on screen, even though the struct is recreated.
struct VarietyDetailView: View {
    let variety: Variety
    @State private var packets = 1
    @State private var remindMe = false

    var body: some View {
        Form {
            LabeledContent("Latin name", value: variety.latinName)
            LabeledContent("Weeks to harvest",
                           value: "\(variety.weeksToHarvest)")
            Stepper("Packets: \(packets)", value: $packets, in: 1...5)
            ReminderToggle(name: variety.commonName, isOn: $remindMe)
            if remindMe {
                Text("We will remind you before month \(firstMonth).")
            }
        }
        .navigationTitle(variety.commonName)
    }

    private var firstMonth: Int { variety.sowingMonths.min() ?? 1 }
}

/// A child view that edits state it does not own: @Binding.
struct ReminderToggle: View {
    let name: String
    @Binding var isOn: Bool

    var body: some View {
        Toggle("Remind me to sow \(name)", isOn: $isOn)
    }
}
#endif
