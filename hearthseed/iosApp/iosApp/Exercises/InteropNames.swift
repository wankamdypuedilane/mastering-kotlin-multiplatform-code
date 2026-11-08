// hearthseed/iosApp/iosApp/Exercises/InteropNames.swift
@preconcurrency import Shared

/// Exercise 16.1: Kotlin declarations of :core:model and :core:data, as
/// Swift sees them through the Objective-C header of Shared.framework.
enum InteropNames {
    /// enum class Difficulty { EASY, MEDIUM, HARD }
    static func label(for difficulty: Difficulty) -> String {
        switch difficulty {
        case .easy: return "Easy"
        case .medium: return "Medium"
        case .hard: return "Hard"
        default: return "Unknown" // a Kotlin enum is a class in Swift
        }
    }

    /// sealed interface DataError, with data object Offline and
    /// data class Server(val status: Int) nested in it.
    static func symbol(for error: any DataError) -> String {
        switch error {
        case is DataErrorOffline:
            return "wifi.slash"
        case let server as DataErrorServer where server.status >= 500:
            return "server.rack"
        default:
            return "exclamationmark.triangle"
        }
    }

    /// Declared in the file UserMessages.kt.
    static func message(for error: any DataError) -> String {
        UserMessagesKt.userMessage(error) // fun DataError.userMessage()
    }

    /// const val DEFAULT_PAGE_SIZE = 20
    static var pageSize: Int32 { // companion object of CatalogRepository
        CatalogRepositoryCompanion.shared.DEFAULT_PAGE_SIZE
    }

    /// The single instance of the data object DataError.Offline.
    static var offline: any DataError { DataErrorOffline.shared }
}
