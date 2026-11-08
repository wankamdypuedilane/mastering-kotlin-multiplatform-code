# Hearthseed: the seed library app of the book

*Hearthseed* is the main project of *Mastering Kotlin Multiplatform*: the member app
of the fictional **Larkmoor Commons Seed Library**. Members browse the catalog of
open-pollinated varieties, look at the seed lots of a variety, borrow a lot and see
their loans. Chapter 11 designs it, Chapter 12 connects it to the local test API
(`../hearthseed-server`), Chapter 14 keeps the catalog on the device with Room, and
later chapters extend it (see "Extension points").

Everything here is fictional; the only token is the demo value `demo-token-cleo`,
which the local test server accepts and which is not a secret.

## Modules

| Module | Kind | What it holds |
|---|---|---|
| `:core:model` | KMP library | Domain types: `Variety`, `SeedLot`, `Loan`, `Member`, `BotanicalFamilies` |
| `:core:network` | KMP library | Ktor client of the API (from Chapter 9): DTOs, `HearthseedApi`, `ApiError` |
| `:core:data` | KMP library | `CatalogRepository`, `LoanRepository`, `DataResult`/`DataError`, mappers, `VarietyLocalSource` (+ in-memory implementation) |
| `:core:database` | KMP library + Room 3 | `HearthseedDatabase` (entities, DAOs, converters, migrations), `RoomVarietyLocalSource`, a database builder per platform, exported schemas in `schemas/` |
| `:core:testing` | KMP library | Fakes and sample data for tests (`FakeCatalogRepository`, `FakeLoanRepository`) |
| `:feature:catalog` | KMP + Compose | Catalog list (search, family filter, paging, refresh) and variety detail: screens and ViewModels |
| `:feature:loans` | KMP + Compose | Borrow flow (POST, 409 handling) and "My loans": screens and ViewModels |
| `:shared` | KMP + Compose | App shell: theme, navigation, Koin wiring, `App()`, iOS `MainViewController`; iOS framework `Shared` |
| `:androidApp` | Android app | `Application` (starts Koin), `MainActivity` (edge-to-edge), `AndroidSharer`, loan reminders (`POST_NOTIFICATIONS`), Robolectric tests in `src/test` |
| `:desktopApp` | JVM app | `main()` with a Compose window |
| `:webApp` | Wasm app (browser) | `ComposeViewport` entry point, web shell with a favorites panel, `index.html` (Chapter 18) |
| `:webApp:support` | KMP library (JVM, JS, Wasm) | Compose-free web logic: favorites in `localStorage`, API base URL, `@JsExport` (JS), Chapter 18 solutions |
| `iosApp/` | Xcode (XcodeGen) | SwiftUI app hosting the Compose UI |
| `:solutions` | KMP + Compose | Exercise solutions of Chapters 11 and 12, with tests (Chapter 14: `ExerciseDao` in `:core:database`) |
| `build-logic/` | included build | Convention plugins `hearthseed.kmp-library`, `hearthseed.kmp-compose`, `hearthseed.android-application`, `hearthseed.module-rules` |

Dependency rules (checked by `./gradlew checkModuleRules`): features never depend on
each other; core modules depend only on core modules; only the app modules use
`:shared`; `:core:testing` appears only in test source sets.

Targets: Android, JVM (desktop), iOS (`iosArm64`, `iosSimulatorArm64`) and `wasmJs`
(the browser app `:webApp`, Chapter 18).

## Local data (Chapter 14)

Android, iOS and the desktop keep the catalog in a Room 3 database, so a variety and
its lots open without the network after a restart. Files: Android
`getDatabasePath("hearthseed.db")`, iOS `Documents/hearthseed.db`, desktop
`~/.hearthseed/hearthseed.db` (delete it to start empty). The web build keeps the
in-memory source (`LocalModule.wasmJs.kt`). Versions: Room `androidx.room3` 3.0.3,
`androidx.sqlite:sqlite-bundled` and `sqlite-async` 2.7.1, KSP 2.3.13 (with Kotlin
2.4.21); the KSP and Room Gradle plugins are on `build-logic`'s classpath, so
`:core:database` applies them by id. Schema history: v1 (variety, lot_refresh,
seed_lot), v2 index on `variety.family` (`AutoMigration(1, 2)`), v3 sowing months
as a bit mask (`MIGRATION_2_3`). Change an entity -> bump `version`, add a migration,
commit the new `schemas/.../N.json`, extend `MigrationTest`.

## Run it with the server

1. Start the test API (port 8085, data reset at every start):

   ```bash
   cd ../hearthseed-server && ./gradlew run
   ```

2. Start a client:

   - Browser: `./gradlew :webApp:wasmJsBrowserDevelopmentRun`, then http://localhost:8080
     (the dev server forwards `/api` to port 8085: the server sends no CORS headers).
   - Desktop: `./gradlew :desktopApp:run` (uses `http://localhost:8085/api/v1/`;
     set `HEARTHSEED_BASE_URL` to use another server; the value must end with `/`).
   - Android emulator: `./gradlew :androidApp:installDebug` (uses `http://10.0.2.2:8085/api/v1/`,
     the emulator's alias of the host's loopback; cleartext HTTP is allowed only for
     `10.0.2.2` and `localhost`).
   - iOS simulator (macOS): `cd iosApp && xcodegen generate && open iosApp.xcodeproj`,
     then run the `iosApp` scheme (uses `http://localhost:8085/api/v1/`;
     `NSAllowsLocalNetworking` allows it).

The base URL of each platform is `defaultBaseUrl()` in
`shared/src/*Main/kotlin/hearthseed/shared/di/BaseUrl.*.kt`.

## Tests

```bash
./gradlew ciLinux -x wasmJsNodeTest -x jsNodeTest -x wasmJsBrowserDistribution
                                      # everything that runs on Linux without npm
./gradlew ciLinux                     # CI: also the Wasm tests of the core modules
./gradlew ciMacos                     # macOS: iOS simulator tests + framework
```

- `:core:network`: `MockEngine` tests of the client.
- `:core:data`: repositories against a `MockEngine` "router" that answers like the server.
- `:core:database`: DAO and local-source tests on an in-memory database
  (`src/bundledTest`, run on the JVM and the iOS simulator); `MigrationTest`
  (JVM, `MigrationTestHelper` reads `schemas/`) and a file database that survives a
  restart. No web tests: the web driver needs a browser worker.
- `:feature:*`: ViewModel tests with fakes and `kotlinx-coroutines-test`;
  `CatalogScreenUiTest` is a desktop UI test (`runComposeUiTest`).
- `:shared`: the Koin graph resolves; `LiveServerUiTest` drives the whole app against a
  running, freshly started server and is skipped unless `HEARTHSEED_E2E_URL` is set:

  ```bash
  HEARTHSEED_E2E_URL=http://localhost:8085/api/v1/ ./gradlew :shared:jvmTest --tests '*LiveServerUiTest*'
  ```

## Extension points (for later chapters)

- **Room (Chapter 14, done):** `RoomVarietyLocalSource` implements `VarietyLocalSource`;
  `initKoin()` passes `platformLocalModule()` (`shared/src/*Main/.../di/LocalModule.*.kt`),
  tests keep `inMemoryLocalModule`. A minified Android build needs the keep rule
  `-keep class * extends androidx.room3.RoomDatabase { <init>(); }`.
- **iOS (Chapter 16):** the framework `Shared` exports `:core:model` and `:core:data`;
  `MainViewControllerKt.doInitKoinIos()` starts Koin. SwiftUI screens can replace
  `ComposeView` screen by screen.
- **Android (Chapter 17, done):** `HearthseedApplication` starts Koin with `androidContext`
  and `BuildConfig.BASE_URL`. `MainActivity` provides the activity-scoped platform actions
  `LocalSharer` and `LocalLoanReminders` (`shared/.../platform/PlatformActions.kt`); a
  platform that provides none hides the Share / Remind me buttons. Build type `device`
  (`assembleDevice`) uses `http://localhost:8085/api/v1/` for a USB phone after
  `adb reverse tcp:8085 tcp:8085`. Host tests: `./gradlew :androidApp:testDebugUnitTest`
  (Robolectric 4.17; the test JVM needs `--add-opens=java.base/jdk.internal.access=ALL-UNNAMED`,
  set in `androidApp/build.gradle.kts`).
- **Web (Chapter 18, done):** `:webApp` (`wasmJs`, `ComposeViewport`) calls `initKoin()`
  plus a web module and hosts `HearthseedNavHost` in `WebShell` next to a favorites
  panel; favorites live in `localStorage` (`:webApp:support`, tested on JVM/JS/Wasm).
  The catalog stays in memory on the web: Room 3 would need `androidx.sqlite:sqlite-web`
  (`WebWorkerSQLiteDriver`) plus a worker script you serve. The test server sends no
  CORS headers, so the webpack dev server proxies `/api` to port 8085.
- **Desktop (Chapter 19):** `desktopApp/.../Main.kt`; window, menus and packaging go there.
- **Testing (Chapter 20):** fakes live in `:core:testing`; the UI and end-to-end tests are
  in `feature/catalog/src/jvmTest` and `shared/src/jvmTest`.
