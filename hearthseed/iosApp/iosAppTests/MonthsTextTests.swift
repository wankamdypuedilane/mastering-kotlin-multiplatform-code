// hearthseed/iosApp/iosAppTests/MonthsTextTests.swift
import Testing
@testable import iosApp

/// Swift Testing in a unit-test bundle hosted by the app (Chapter 16).
struct MonthsTextTests {
    @Test func joinsConsecutiveMonthsIntoRanges() {
        #expect(monthsText([9, 3, 4, 5]) == "3-5, 9")
    }

    @Test func dropsDuplicatesAndInvalidMonths() {
        #expect(monthsText([4, 4, 0, 13]) == "4")
    }

    @Test(arguments: [([Int](), ""), ([1, 12], "1, 12"), ([2, 3], "2-3")])
    func formats(months: [Int], expected: String) {
        #expect(monthsText(months) == expected)
    }
}
