# Hearthseed: the seed library app of the book

*Hearthseed* is the main project of *Mastering Kotlin Multiplatform*: the member app
of the fictional **Larkmoor Commons Seed Library**. Members browse the catalog of
open-pollinated varieties, look at the seed lots of a variety, borrow a lot and see
their loans. Chapter 11 designs it, Chapter 12 connects it to the local test API
(`../hearthseed-server`), and later chapters extend it (see "Extension points").

Everything here is fictional; the only token is the demo value `demo-token-cleo`,
which the local test server accepts and which is not a secret.

## Modules

| Module | Kind | What it holds |
|---|---|---|
| `:core:model` | KMP library | Domain types: `Variety`, `SeedLot`, `Loan`, `Member`, `BotanicalFamilies` |
| `:core:network` | KMP library | Ktor client of the API (from Chapter 9): DTOs, `HearthseedApi`, `ApiError` |
| `:core:data` | KMP library | `CatalogRepository`, `LoanRepository`, `DataResult`/`DataError`, mappers, `VarietyLocalSource` (+ in-memory implementation) |
| `:core:testing` | KMP library | Fakes and sample data for tests (`FakeCatalogRepository`, `FakeLoanRepository`) |
| `:feature:catalog` | KMP + Compose | Catalog list (search, family filter, paging, refresh) and variety detail: screens and ViewModels |
| `:feature:loans` | KMP + Compose | Borrow flow (POST, 409 handling) and "My loans": screens and ViewModels |
| `:shared` | KMP + Compose | App shell: theme, navigation, Koin wiring, `App()`, iOS `MainViewController`; iOS framework `Shared` |
| `:androidApp` | Android app | `Application` (starts Koin) and `MainActivity` |
| `:desktopApp` | JVM app | `main()` with a Compose window |
| `iosApp/` | Xcode (XcodeGen) | SwiftUI app hosting the Compose UI |
| `:solutions` | KMP + Compose | Exercise solutions of Chapters 11 and 12, with tests |
| `build-logic/` | included build | Convention plugins `hearthseed.kmp-library`, `hearthseed.kmp-compose`, `hearthseed.android-application`, `hearthseed.module-rules` |

Dependency rules (checked by `./gradlew checkModuleRules`): features never depend on
each other; core modules depend only on core modules; only the app modules use
`:shared`; `:core:testing` appears only in test source sets.

Targets: Android, JVM (desktop), iOS (`iosArm64`, `iosSimulatorArm64`) and `wasmJs`
(compiled only; the web app comes in Chapter 18).

## Run it with the server

1. Start the test API (port 8085, data reset at every start):

   ```bash
   cd ../hearthseed-server && ./gradlew run
   ```

2. Start a client:

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
./gradlew ciLinux -x wasmJsNodeTest   # everything that runs on Linux without Node.js
./gradlew ciLinux                     # CI: also the Wasm tests of the core modules
./gradlew ciMacos                     # macOS: iOS simulator tests + framework
```

- `:core:network`: `MockEngine` tests of the client.
- `:core:data`: repositories against a `MockEngine` "router" that answers like the server.
- `:feature:*`: ViewModel tests with fakes and `kotlinx-coroutines-test`;
  `CatalogScreenUiTest` is a desktop UI test (`runComposeUiTest`).
- `:shared`: the Koin graph resolves; `LiveServerUiTest` drives the whole app against a
  running, freshly started server and is skipped unless `HEARTHSEED_E2E_URL` is set:

  ```bash
  HEARTHSEED_E2E_URL=http://localhost:8085/api/v1/ ./gradlew :shared:jvmTest --tests '*LiveServerUiTest*'
  ```

## Extension points (for later chapters)

- **Room (Chapter 14):** implement `VarietyLocalSource` (`core/data/.../local/`) with Room
  in a new `:core:database` module and pass its Koin module as `localModule` to
  `hearthseedModules(config, localModule)`. Repositories, ViewModels and UI do not change.
- **iOS (Chapter 16):** the framework `Shared` exports `:core:model` and `:core:data`;
  `MainViewControllerKt.doInitKoinIos()` starts Koin. SwiftUI screens can replace
  `ComposeView` screen by screen.
- **Android (Chapter 17):** `HearthseedApplication` starts Koin with `androidContext`.
- **Web (Chapter 18):** `:shared` already compiles for `wasmJs` (browser). A `:webApp` module
  needs a `ComposeViewport` entry point and `initKoin()`. The test server sends no CORS
  headers, so a page served from another origin needs a development proxy.
- **Desktop (Chapter 19):** `desktopApp/.../Main.kt`; window, menus and packaging go there.
- **Testing (Chapter 20):** fakes live in `:core:testing`; the UI and end-to-end tests are
  in `feature/catalog/src/jvmTest` and `shared/src/jvmTest`.
