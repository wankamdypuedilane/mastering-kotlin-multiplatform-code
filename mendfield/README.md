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
| 2 | `:core:database` (Room), `:feature:*` (Compose UI), `:shared`, `:androidApp`, `:desktopApp`, `:webApp`, `iosApp/` (SwiftUI shell), Koin wiring | next |

## Modules

```text
:server ──────────────► :core:domain ──► :core:model
   (Ktor, JVM)                              ▲
:core:sync ──► :core:network ───────────────┘
   (outbox)      (Ktor client, DTOs)
```

| Module | Plugin | Targets | What it holds |
|---|---|---|---|
| `:core:model` | `mendfield.kmp-library` | Android, JVM, JS, Wasm, iOS | `Event`, `Fixer`, `Skill`, `Item`, `ItemCategory`, `RepairSlot`, `Outcome`, `OutcomeKind`. Pure data; depends only on kotlinx-datetime. |
| `:core:domain` | `mendfield.kmp-library` | same | Business rules, pure functions: time windows, slot grid, slot and assignment rules, triage, queue planning, outcome statistics. |
| `:core:network` | `mendfield.kmp-library` + serialization | same | `MendfieldApi` (Ktor client), DTOs, mappers, `ApiError`/`ApiResult`, `createHttpClient`. |
| `:core:sync` | `mendfield.kmp-library` | same | Outbox: `Operation`, `OutboxStore` (+ `InMemoryOutboxStore`), `Outbox`, `SyncEngine`, `RetryPolicy`, `ApiSyncTransport`, `OperationCodec`. |
| `:server` | `mendfield.jvm-server` | JVM | The local API on port 8086 (see `server/README.md`). |

The rules between modules are data in
`build-logic/src/main/kotlin/mendfield/buildlogic/ModuleRules.kt` and are
checked by `./gradlew checkModuleRules`: the model depends on no module, the
pure modules (`model`, `domain`) never on an I/O module, core modules only on
core modules, the server only on `model` and `domain`, features never on each
other.

Convention plugins (`build-logic/src/main/kotlin`):
- `mendfield.kmp-library`: KMP + Android-KMP library; targets `android`
  (namespace from the path, `compileSdk` 37, `minSdk` 24, host tests on),
  `jvm`, `js { nodejs() }`, `wasmJs { nodejs() }`, `iosArm64`,
  `iosSimulatorArm64`; `kotlin("test")` in `commonTest`.
- `mendfield.jvm-server`: Kotlin/JVM + serialization + `application`.
- `mendfield.module-rules`: the `checkModuleRules` task (root project).

## The public API that part 2 builds on

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
| `AssignSlot` | First writer wins. Another item in the slot, the item already elsewhere, or the visitor busy at another table: rejected (`slot_taken`, `item_scheduled`, `visitor_busy`). The app refreshes its copy from the server (part 2); nothing is re-scheduled automatically. |
| `LogOutcome` | Append-only, never a conflict. Several outcomes of one item may exist; statistics use the latest by `loggedAt` (device time), then by id. |

Operations are applied in queue order, so an item is registered before it is
assigned. If its registration is rejected, the assignment is rejected too
(`not_found`).

## Time and time zones

Events store a `LocalDate`, wall-clock `LocalTime`s and the venue's
`TimeZone`; slots and timestamps are `kotlin.time.Instant`s. All conversions
go through `Event.window`, so an event on a daylight-saving day has the right
length (JVM tests in `core/domain/src/jvmTest`). Common tests use a fixed
UTC+01:00 offset because, on JS and Wasm, `TimeZone.of("Europe/London")`
throws unless the app loads the `@js-joda/timezone` library: the web app of
part 2 must add it.

## Run and test

```bash
./gradlew :server:run      # the API on http://127.0.0.1:8086
./gradlew ciLinux          # what CI runs on Linux (see below)
./gradlew ciMacos          # iOS simulator tests, on macOS only
./gradlew checkModuleRules
```

- `ciLinux`: `allTests` of the four core modules (JVM, Android host, JS and
  Wasm on Node.js), `compileAndroidMain`, `:server:test`, the build-logic
  tests and `checkModuleRules`.
- `ciMacos`: `iosSimulatorArm64Test` of the four core modules.

Tests: `:core:model` 5, `:core:domain` 46 common + 3 JVM-only, `:core:network`
18, `:core:sync` 23 (each common test runs on every target), `:server` 18,
`build-logic` 3.
