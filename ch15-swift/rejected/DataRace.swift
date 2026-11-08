// ch15-swift/rejected/DataRace.swift (does NOT compile in Swift 6 mode)
final class TrayCounter {
    var sprouted = 0
}

func countTrays() async {
    let counter = TrayCounter()          // a mutable class: not Sendable
    await withTaskGroup(of: Void.self) { group in
        for _ in 1...4 {
            group.addTask {
                counter.sprouted += 1    // four tasks, one variable
            }
        }
    }
}
