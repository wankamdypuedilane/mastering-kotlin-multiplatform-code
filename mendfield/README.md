# Mendfield: the capstone project

Mendfield is the repair-café organizer of Chapter 23 of *Mastering Kotlin
Multiplatform*, for the fictional *Brindle Street Repair Café*. Volunteers
("fixers") have skills; visitors register broken items; the organizer
schedules repair slots at an event; triage rules suggest a fixer; outcomes
(FIXED, PARTIAL, NOT_FIXED, ADVICE) are logged; the app works offline and
syncs an outbox of changes when it is back online.

This is a standalone Gradle build (Gradle 9.7.1, Kotlin 2.4.21, AGP 9.3.1,
JDK 21), started from `code/toolchain-check` with the `build-logic`
convention plugins of `code/hearthseed`.

## Status

| Part | Contents | State |
|---|---|---|
| 1 | `build-logic`, `:core:model`, `:core:domain`, `:core:network`, `:core:sync`, `:server` | done |
| 2 | `:core:data`, `:core:database` (Room 3), `:core:ui`, `:core:testing`, `:feature:events`, `:feature:intake`, `:feature:outcomes`, `:shared`, `:androidApp`, `:desktopApp`, `:webApp`, `iosApp/` (SwiftUI shell) | done |
| 3 | `:solutions`: the solutions of the chapter's exercises (`freeCapacity`, `pendingByEvent`, `ConflictResolver`), tested on every target | done |

## Modules

```text
 :androidApp  :desktopApp  :webApp  iosApp/ (Xcode, framework "Shared")
        \           |         |        /
         └───────► :shared (App(), navigation, sync bar, Koin, TodayFeed)
                     │
   :feature:events  :feature:intake  :feature:outcomes   (never each other)
                     │
                 :core:ui (theme, labels, banners)
                     │
 :core:database ──► :core:data ──► :core:sync ──► :core:network ──► :core:model
 (Room: Android,     (repositories,  (outbox)       (Ktor client)       ▲
  JVM, iOS only)      offline-first)                                    │
                       └──────────► :core:domain ───────────────────────┘
 :server (Ktor, JVM) ──► :core:domain, :core:model
 :core:testing (fakes, contracts) ── test source sets only
```

| Module | Plugin | Targets | What it holds |
|---|---|---|---|
| `:core:model` | `mendfield.kmp-library` | Android, JVM, JS, Wasm, iOS | `Event`, `Fixer`, `Skill`, `Item`, `ItemCategory`, `RepairSlot`, `Outcome`, `OutcomeKind`. Pure data; depends only on kotlinx-datetime. |
| `:core:domain` | `mendfield.kmp-library` | same | Business rules, pure functions: time windows, slot grid, slot and assignment rules, triage, queue planning, outcome statistics. |
| `:core:network` | `mendfield.kmp-library` + serialization | same | `MendfieldApi` (Ktor client), DTOs, mappers, `ApiError`/`ApiResult`, `createHttpClient`. |
| `:core:sync` | `mendfield.kmp-library` | same | Outbox: `Operation`, `OutboxStore` (+ `InMemoryOutboxStore`), `Outbox`, `SyncEngine`, `RetryPolicy`, `ApiSyncTransport`, `OperationCodec`. |
| `:core:data` | `mendfield.kmp-library` + serialization | same | `CafeCache` (+ `InMemoryCafeCache`), `CafeRemote`/`ApiCafeRemote`, `LocalWriter`, `withPending` (rebase), `EventRepository`, `IntakeRepository`, `OutcomeRepository`, `ConnectivityMonitor` (`ProbingConnectivityMonitor`, `ManualConnectivity`), `SyncCoordinator`/`SyncStatus`, `KeyValueOutboxStore` + `BrowserStorage` (web), `slotSuggestions`, `draftProblems`. |
| `:core:database` | `mendfield.kmp-room` | Android, JVM, iOS | Room 3: `MendfieldDatabase` (tables `event`, `fixer`, `item`, `slot`, `outcome`, `outbox`), `CafeDao`, `OutboxDao`, `Converters`, `RoomCafeCache`, `RoomOutboxStore`, a builder per platform, `schemas/`. |
| `:core:testing` | `mendfield.kmp-library` | all five | `SampleData`, `FakeCafeServer` (reads + outbox, with the domain rules), `TestCafe` (the data layer wired by hand), `TestClock`, `sequentialIds()`, `CafeCacheContract`, `OutboxStoreContract`. |
| `:core:ui` | `mendfield.kmp-compose` | Android, JVM, Wasm, iOS | `MendfieldTheme`, labels and time formatting, `ErrorBanner`, `FullScreenError`, `FullScreenLoading`. |
| `:feature:events` | `mendfield.kmp-compose` | same | Event list and the event board (counts, waiting items, triage suggestions, slots per fixer): `EventListViewModel`, `EventBoardViewModel`, `boardContent`, screens. |
| `:feature:intake` | `mendfield.kmp-compose` | same | Register an item, with live triage hints: `IntakeViewModel`, `IntakeScreen`. |
| `:feature:outcomes` | `mendfield.kmp-compose` | same | Log an outcome; statistics per event: `LogOutcomeViewModel`, `EventStatsViewModel`, screens. |
| `:shared` | `mendfield.kmp-compose` + serialization | same | `App()`, type-safe routes and `MendfieldNavHost`, `SyncStatusBar`/`SyncStatusViewModel`, Koin modules and `initKoin`, `TodayFeed` (the Swift-facing API), `MainViewController`; iOS framework `Shared`. |
| `:androidApp` | `mendfield.android-application` | Android | `MendfieldApplication` (Koin with `androidContext`), `MainActivity`. |
| `:desktopApp` | KMP `jvm()` + Compose | JVM | `main()`: one window; closes Koin (Room, HTTP client) on exit. |
| `:webApp` | KMP `wasmJs { browser() }` + Compose | Wasm | `ComposeViewport`, `@js-joda/timezone` (npm), dev-server proxy to the API. |
| `iosApp/` | XcodeGen | iOS | SwiftUI `TabView`: a native "Today" screen over `TodayFeed`, and the Compose app in the second tab. |
| `:server` | `mendfield.jvm-server` | JVM | The local API on port 8086 (see `server/README.md`). |

The rules between modules are data in
`build-logic/src/main/kotlin/mendfield/buildlogic/ModuleRules.kt` and are
checked by `./gradlew checkModuleRules`: the model depends on no module, the
pure modules (`model`, `domain`) never on an I/O module, core modules only on
core modules, the server only on `model` and `domain`, features never on each
other nor on `:core:database`, only the app modules on `:shared`, and nothing
on `:core:testing` outside test source sets.

Convention plugins (`build-logic/src/main/kotlin`):
- `mendfield.kmp-library`: KMP + Android-KMP library; targets `android`
  (namespace from the path, `compileSdk` 37, `minSdk` 24, host tests on),
  `jvm`, `js { nodejs() }`, `wasmJs { nodejs() }`, `iosArm64`,
  `iosSimulatorArm64`; `kotlin("test")` in `commonTest`.
- `mendfield.kmp-compose`: the same without JS, with `wasmJs { browser() }`,
  the Compose compiler and plugin, Material 3, lifecycle ViewModel and
  runtime-compose; desktop UI tests (`ui-test`, `compose.desktop.currentOs`)
  in `jvmTest`.
- `mendfield.kmp-room`: `android`, `jvm`, `iosArm64`, `iosSimulatorArm64`
  only, KSP + `androidx.room3`, `room3-runtime` and `sqlite-bundled`, one
  `ksp<Target>` configuration per target, `room3 { schemaDirectory(...) }`,
  `-Xexpect-actual-classes`. No Android host tests (the bundled driver's
  Android build has device code only): `commonTest` runs on JVM and iOS.
- `mendfield.android-application`: `com.android.application` + Compose
  compiler, `compileSdk` 37, `minSdk` 24, `targetSdk` 36.
- `mendfield.jvm-server`: Kotlin/JVM + serialization + `application`.
- `mendfield.module-rules`: the `checkModuleRules` task (root project).

Versions added in part 2 (all already used and verified by `code/hearthseed`):
Compose Multiplatform 1.12.2, Material 3 1.12.0-alpha03, Koin 4.2.2,
lifecycle 2.11.0, navigation-compose 2.10.0, activity-compose 1.12.4,
Room `androidx.room3` 3.0.3, `androidx.sqlite` 2.7.1, KSP 2.3.13,
kotlinx-browser 0.5.0. New: the npm package `@js-joda/timezone` **2.23.0**
(see "Time and time zones").

## The public API of part 1

`:core:domain` (package `mendfield.core.domain`):
- `TimeWindow(start, end)` with `overlaps`, `contains`, `duration`;
  `RepairSlot.window`; `Event.hours()`; `Event.window(from, until)`.
- `slotGrid(event, fixerId, length, from = opensAt, until = closesAt)` and
  `slotId(eventId, fixerId, n)`.
- `checkNewSlot(event, existing, candidate): SlotProblem?` and
  `checkAssignment(slot, item, eventSlots, eventItems): AssignmentProblem?`.
- `suggestFixers(category, fixers, eventSlots): List<FixerSuggestion>`
  (`SkillMatch.PRIMARY` before `RELATED`, then lighter load, then more
  related skills, then name and id).
- `planQueue(items, fixers, slots, notBefore): QueuePlan` (first registered,
  first served; best fixer with a usable slot, earliest slot of that fixer).
- `outcomeStats(items, outcomes): OutcomeStats`.

`:core:network` (package `mendfield.core.network`):
- `createHttpClient(ApiConfig(baseUrl = "http://127.0.0.1:8086/api/v1/"))`,
  `MendfieldApi(client, organizerToken)` with `events`, `event`, `slots`,
  `items`, `outcomes`, `stats`, `fixers`, `registerItem`, `assignSlot`,
  `logOutcome`, `sync`; reads return models, every call returns
  `ApiResult<T>`; `ApiError.isTransient`.

`:core:sync` (package `mendfield.core.sync`):
- `Outbox(store, clock, newId).enqueue(Operation)`, `newEntityId()`.
- `OutboxStore`: `pending`/`rejected` flows, `add`, `oldest`, `remove`,
  `reject`, `dismiss`. Part 2 adds a Room implementation (and a
  localStorage one for the web), storing `OperationCodec.encode(op)` and
  `createdAt` per row.
- `SyncEngine(store, transport, policy, batchSize)`: `syncNow()`,
  `launchIn(scope, online)`, `state: StateFlow<SyncState>`.
- `ApiSyncTransport(api)` is the real `SyncTransport`.

## Offline sync and the conflict policy

Every change made on the device is first written locally and queued in the
outbox as an `Operation` (`RegisterItem`, `AssignSlot`, `LogOutcome`) with a
fresh UUID, its **idempotency key**. Items and outcomes created on the device
get client-generated ids too, so they keep them on the server.

`SyncEngine` sends the oldest operations in batches (20 by default, the
server accepts 50) to `POST /api/v1/sync`:

- **Transient failures** (no network, timeout, HTTP 5xx, a throwing
  transport): the same batch is resent after 1 s, 2 s, 4 s... (`RetryPolicy`,
  5 attempts, at most 1 minute apart). The operations keep their ids, so a
  batch whose response was lost is applied only once: the server answers a
  replay with the stored result. After the last attempt the engine reports
  `GaveUp` and keeps everything queued.
- **Permanent failures** (401, a malformed batch): no retry; `Stopped`; the
  operations stay queued until the problem is fixed.
- **Per-operation results:** APPLIED operations leave the outbox. REJECTED
  operations move to the `rejected` list with the server's code; the organizer
  sees them and dismisses them. Nothing is retried or merged automatically.
- `launchIn` syncs whenever the device is online and the newest queued
  operation changes: on reconnection, and when work is added while online.

Conflict policy (the server is the authority):

| Operation | Policy |
|---|---|
| `RegisterItem` | Client id; the same id with the same data is a no-op; other data under that id is rejected (`id_conflict`). |
| `AssignSlot` | First writer wins. Another item in the slot, the item already elsewhere, or the visitor busy at another table: rejected (`slot_taken`, `item_scheduled`, `visitor_busy`). `SyncCoordinator` then refreshes that event from the server, so the board shows the winner; nothing is re-scheduled automatically. |
| `LogOutcome` | Append-only, never a conflict. Several outcomes of one item may exist; statistics use the latest by `loggedAt` (device time), then by id. |

Operations are applied in queue order, so an item is registered before it is
assigned. If its registration is rejected, the assignment is rejected too
(`not_found`).

## The offline-first data layer (part 2)

The device copy is the **single source of truth** of every screen:
repositories expose `Flow`s that read only the `CafeCache` (Room on
Android, iOS and desktop), and the network only ever writes into it.

- **Reads:** `EventRepository.events`, `fixers`, `board(eventId)` (event +
  fixers + slots + items + outcomes, combined off the main thread),
  `OutcomeRepository.stats(eventId)` and `itemHistory(itemId)`. Room's
  `eventData` re-reads the three tables of an event in one `@Transaction`
  whenever the invalidation tracker reports a write, so a refresh is one
  emission, never a mix of old and new rows (`aReplaceIsOneEmissionNotAHalfReplacedBoard`).
- **Writes** (`IntakeRepository.registerItem`, `assign`,
  `OutcomeRepository.logOutcome`) check the rules of `:core:domain` against
  the device copy, then go through `LocalWriter`: **outbox first, then
  cache**. If the app dies in between, the operation is still queued and the
  next refresh replays it; the other order could lose a change. They never
  wait for the network, so intake works the same offline.
- **Refresh** (`refreshEvents`, `refreshEvent`) replaces one event's rows in
  one transaction with the server's copy **rebased on the unsent operations**
  (`EventData.withPending`): an item registered offline does not vanish
  from the board while it waits in the outbox. `LocalWriter` serializes
  writes and refreshes with a mutex, so a refresh never overwrites a change
  made while it was loading. A failed refresh changes nothing on screen; the
  ViewModels show an `ErrorBanner` with Retry above the cached data, or a
  full-screen error with Retry when the device knows nothing yet.
- **Sync:** `SyncCoordinator.start(scope)` runs the connectivity monitor and
  `SyncEngine.launchIn` (part 1), refreshes the event list after every
  reconnection, and refreshes every event whose operations the server
  refused. `status: Flow<SyncStatus>` (online, pending count, refused
  operations, engine state, "working offline") feeds the sync bar at the
  bottom of every screen: "Offline · 3 changes waiting", "Sending 2
  changes…", "No answer, retrying in 4 s…", "All changes sent", plus
  "Sync now", a "work offline" switch and a dialog to review and dismiss
  refused operations.

**Connectivity, the choice made here:** one common
`ProbingConnectivityMonitor` that asks the server itself (`GET /health`)
every 30 s, every 10 s while it gets no answer, and at once after "Sync now"
or when "work offline" is switched off. Operating-system callbacks
(`ConnectivityManager`, `NWPathMonitor`, `navigator.onLine`) only say that a
network exists, not that the café's server is reachable through it; the
probe answers the real question, works the same on all targets, needs no
permission, and is tested in virtual time (`ConnectivityTest`). The cost: a
small request every 30 s while the app runs, and no background sync once
the process is gone (see the checklist).

## Time and time zones

Events store a `LocalDate`, wall-clock `LocalTime`s and the venue's
`TimeZone`; slots and timestamps are `kotlin.time.Instant`s. All conversions
go through `Event.window`, so an event on a daylight-saving day has the right
length (JVM tests in `core/domain/src/jvmTest`). Common tests use a fixed
offset or UTC because, on JS and Wasm, `TimeZone.of("Europe/London")` throws
unless the app loads the `@js-joda/timezone` npm package. `:webApp` does:
`implementation(npm("@js-joda/timezone", "2.23.0"))` and the
`@JsModule("@js-joda/timezone") external object` of the kotlinx-datetime
README (`webApp/src/wasmJsMain/kotlin/mendfield/web/Main.kt`). The version
is pinned on purpose: kotlinx-datetime 0.8.0 depends on `@js-joda/core`
3.2.0, and 2.23.0 is the newest `@js-joda/timezone` whose peer dependency
accepts it (2.24.0 and later, including 3.x, require `@js-joda/core` 5.7.0
or newer; checked on registry.npmjs.org, 8 November 2026).

Room stores instants as nanoseconds since 1970 in one `INTEGER` column
(`Converters.instantToNanos`): lossless (the server sends nanoseconds) and
sortable in SQL, for the years 1677 to 2262.

## Target compatibility matrix

| | Android | iOS | Desktop (JVM) | Web (Wasm) |
|---|---|---|---|---|
| UI | Compose | SwiftUI "Today" tab + Compose app tab | Compose | Compose (`ComposeViewport`, CMP web is Beta) |
| Device copy (`CafeCache`) | Room 3, file in the app's databases folder | Room 3, `Documents/mendfield.db` | Room 3, `~/.mendfield/mendfield.db` | **memory only** (`InMemoryCafeCache`): a reload starts empty and refetches |
| Outbox (`OutboxStore`) | Room table `outbox` | Room table `outbox` | Room table `outbox` | **`localStorage`** (`KeyValueOutboxStore`, key `mendfield.outbox`); memory if the browser refuses storage |
| Works offline after a restart | yes: board, stats and queued work | yes | yes | queued work survives a reload; the board does not (empty until the server answers) |
| Cache and outbox in one transaction | yes (one database) | yes | yes | no (two stores) |
| Region time zones | built in | built in | built in | `@js-joda/timezone` npm package |
| Reaching the dev server | `http://10.0.2.2:8086` (emulator), cleartext allowed for it and `localhost` only | `http://localhost:8086` (simulator), `NSAllowsLocalNetworking` | `http://localhost:8086`, or `MENDFIELD_BASE_URL` | the page's origin; the webpack dev server proxies `/api` and `/health` to 8086 (the server sends no CORS headers) |
| Tests that run | host tests of the Compose-free modules; `assembleDebug` | simulator tests of every module (CI, macOS) | everything on the JVM, including the desktop UI flow test | Node.js tests of the Compose-free modules (CI); the app is compiled and bundled, not run |

Why no Room on the web: Room 3 does publish JS and Wasm artifacts, but its
web driver (`WebWorkerSQLiteDriver` from `androidx.sqlite:sqlite-web`) needs
a worker script that the app must write and serve, and `sqlite-bundled` has
no web artifact. `:core:database` therefore targets Android, JVM and iOS
only, and the web gets the two small stores above.

## Performance notes

- UI state classes are immutable `data class`es marked `@Immutable` (they
  hold `List`s, which the Compose compiler cannot prove stable): equal states
  skip recomposition.
- Everything a screen shows is computed in the ViewModel, once per change of
  the data (`boardContent`, `toSummary`, `syncBarState`): counts are derived
  values, composition never filters, sorts or formats.
- Combining and mapping run with `flowOn(compute)` (`Dispatchers.Default` in
  the app, the test dispatcher in tests); Room runs its queries on
  `Dispatchers.IO` (`setQueryCoroutineContext`); writes are suspend calls from
  `viewModelScope`. Nothing blocks the main thread.
- Every `LazyColumn` item has a stable key (event id, `waiting-<item>`, slot
  id), so an assignment updates one row in place.
- `stateIn(WhileSubscribed(5_000))`: Room is observed only while a screen
  is visible (and through a rotation); `distinctUntilChanged` drops equal
  emissions; indexes on `event_id` and `item_id` serve every board query.

## Run and test

```bash
./gradlew :server:run                 # the API on http://127.0.0.1:8086
./gradlew :desktopApp:run             # the desktop app (MENDFIELD_BASE_URL to change)
./gradlew :androidApp:installDebug    # the Android app, on an emulator
./gradlew :webApp:wasmJsBrowserDevelopmentRun   # http://localhost:8080
cd iosApp && xcodegen generate && open iosApp.xcodeproj   # macOS: run "iosApp"
./gradlew ciLinux                     # what CI runs on Linux (see below)
./gradlew ciMacos                     # iOS simulator tests, on macOS only
./gradlew checkModuleRules
```

- `ciLinux`: `allTests` of the Compose-free modules (`model`, `domain`,
  `network`, `sync`, `data`, `testing`: JVM, Android host, JS and Wasm on
  Node.js), `compileAndroidMain` of those and of `:core:database`,
  `:core:database:jvmTest`, `jvmTest` of `:core:ui`, the three features and
  `:shared` (including the desktop UI flow test), `:server:test`,
  `:androidApp:assembleDebug`, `:desktopApp:compileKotlinJvm`,
  `:webApp:compileKotlinWasmJs` and `:webApp:wasmJsBrowserDistribution`, the
  build-logic tests and `checkModuleRules`.
- `ciMacos`: `iosSimulatorArm64Test` of every library, database and Compose
  module, and `:shared:linkDebugFrameworkIosSimulatorArm64`.

Tests (test functions; a common test runs on every target of its module):

| Module | Tests | Runs on |
|---|---|---|
| `:core:model` | 5 | JVM, Android host, JS, Wasm, iOS |
| `:core:domain` | 46 common + 3 JVM-only (daylight saving) | same |
| `:core:network` | 18 | same |
| `:core:sync` | 23 | same |
| `:core:data` | 24 (cache and outbox contracts, rebase, repositories, connectivity in virtual time, sync coordinator) | same |
| `:core:database` | 11 common (Room contracts, one-emission refresh, converters) + 1 JVM-only (file database survives a restart) | JVM, iOS |
| `:core:ui` | 2 | JVM, iOS |
| `:feature:events` | 5 (ViewModels) | JVM, iOS |
| `:feature:intake` | 3 | JVM, iOS |
| `:feature:outcomes` | 3 | JVM, iOS |
| `:shared` | 4 common (sync bar, TodayFeed) + 4 JVM-only (Koin graph with HTTP and with Room, base URL, the desktop UI flow test) | JVM (+ iOS for the common ones) |
| `:server` | 18 | JVM |
| `build-logic` | 5 | JVM |

`OrganizerFlowUiTest` (`shared/src/jvmTest`) drives the real app on the
desktop with a `FakeCafeServer`: open the November event, switch to
"work offline", register an item (with its triage hint), give it the slot
triage suggests, log its outcome, check the statistics, reconnect, and see
the three queued operations reach the server in order.

## Release-preparation checklist

Before Mendfield could leave the café's back room (see Chapter 22):

- [ ] **Identity and secrets:** replace the demo organizer token
      (`DEMO_ORGANIZER_TOKEN`, accepted only by the local server) with a real
      sign-in; store the session token in the Android Keystore / iOS Keychain.
- [ ] **Transport:** serve the API over HTTPS; remove the cleartext rules
      (`network_security_config.xml`) and `NSAllowsLocalNetworking`; point
      `BASE_URL`/`defaultBaseUrl()` at the real host per build type.
- [ ] **Web:** add CORS to the server (or serve the page from the API's
      origin) instead of the development proxy; decide whether the board
      should persist on the web (Room's `WebWorkerSQLiteDriver` + a worker
      script, or a `localStorage` snapshot) and say so in the matrix above.
- [ ] **Database:** commit `core/database/schemas/` for every version; any
      entity change bumps `version` with a migration and a migration test.
      Turn R8 on (`isMinifyEnabled = true` in `androidApp/build.gradle.kts`,
      off for now) with the rule `-keep class * extends
      androidx.room3.RoomDatabase { <init>(); }` that
      `androidApp/proguard-rules.pro` already holds, and test the minified
      build on a device: no CI job builds it.
- [ ] **Background sync:** the outbox drains only while the app runs. For
      delivery after the app is closed, schedule work (WorkManager on Android,
      `BGTaskScheduler` on iOS) that calls `SyncCoordinator.syncNow()`.
- [ ] **Privacy:** items carry visitors' names. Write the data-safety form
      (Play) and the privacy manifest (`PrivacyInfo.xcprivacy`, including the
      required-reason APIs) and a retention rule for old events.
- [ ] **Versions:** `versionCode`/`versionName` (Android), `CFBundleVersion`/
      `CFBundleShortVersionString` (iOS), `packageVersion` (desktop) from one
      place; `targetSdk` at the Play requirement (36 today).
- [ ] **Packaging:** Android App Bundle signed with an upload key
      (`bundleRelease`); iOS archive with Xcode 26+ and the iOS 26 SDK;
      desktop `packageDmg`/`packageMsi`/`packageDeb` on each OS after
      `suggestRuntimeModules`; web `wasmJsBrowserDistribution` served with
      the `application/wasm` MIME type and cache headers.
- [ ] **Quality gates:** `ciLinux` and `ciMacos` green; the UI flow test
      and an exploratory run against `:server` on every platform; logging
      without visitors' names or the token.
- [ ] **Accessibility:** content descriptions for icon-only controls if any
      are added, touch targets of 48 dp, text that scales; check VoiceOver
      (iOS) and TalkBack (Android).
