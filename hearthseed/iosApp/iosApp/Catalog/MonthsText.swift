// hearthseed/iosApp/iosApp/Catalog/MonthsText.swift
/// Months as short text: [3, 4, 5, 9] -> "3-5, 9". Pure Swift, no Kotlin
/// types, so the unit tests in iosAppTests need no shared ViewModel.
func monthsText(_ months: [Int]) -> String {
    let sorted = Set(months).filter { (1...12).contains($0) }.sorted()
    var runs: [ClosedRange<Int>] = []
    for month in sorted {
        if let last = runs.last, last.upperBound == month - 1 {
            runs[runs.count - 1] = last.lowerBound...month
        } else {
            runs.append(month...month)
        }
    }
    return runs.map { run in
        run.count == 1 ? "\(run.lowerBound)"
                       : "\(run.lowerBound)-\(run.upperBound)"
    }.joined(separator: ", ")
}
