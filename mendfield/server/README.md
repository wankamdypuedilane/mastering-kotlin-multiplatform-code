# Mendfield server: the local API of the capstone

A small Ktor server (JVM, Netty, port **8086**) that plays the role of the
*Brindle Street Repair Café* back end in Chapter 23 of *Mastering Kotlin
Multiplatform*. Everything in it is fictional: 3 events, 8 volunteer fixers,
14 registered items and 10 outcomes, loaded from
`src/main/resources/dataset.json` into memory. Every restart resets the data.

It is a module of the Mendfield build (`:server`) and depends on
`:core:model` and `:core:domain`: the slot grid, the assignment rules
(`checkAssignment`) and the statistics (`outcomeStats`) are the same code
the apps run offline.

## Run

```bash
./gradlew :server:run     # http://127.0.0.1:8086, stop with Ctrl+C
./gradlew :server:test    # testApplication tests of every endpoint
```

The server listens on the loopback interface only (127.0.0.1), because its
token check is not real security. Android emulators reach it at
`http://10.0.2.2:8086`, the iOS simulator and desktop programs at
`http://localhost:8086`.

## Contract

| Method and path | Success | Errors |
|---|---|---|
| `GET /health` (no token) | 200 `{"status":"ok","events":3}` | |
| `GET /api/v1/events` | 200 `List<EventDto>`, by date | 401 |
| `GET /api/v1/events/{id}` | 200 `EventDto` | 401, 404 |
| `GET /api/v1/events/{id}/slots` | 200 `List<RepairSlotDto>` | 401, 404 |
| `GET /api/v1/events/{id}/items` | 200 `List<ItemDto>` | 401, 404 |
| `GET /api/v1/events/{id}/outcomes` | 200 `List<OutcomeDto>` | 401, 404 |
| `GET /api/v1/events/{id}/stats` | 200 `StatsDto` | 401, 404 |
| `GET /api/v1/fixers` | 200 `List<FixerDto>` | 401 |
| `POST /api/v1/items` body `NewItemDto` | 201 `ItemDto` (200 if the same id and data exist) | 400, 401, 404, 409 |
| `POST /api/v1/slots/{id}/assign` body `{"itemId"}` | 200 `RepairSlotDto` | 400, 401, 404, 409 |
| `POST /api/v1/outcomes` body `NewOutcomeDto` | 201 `OutcomeDto` (200 if the same id and data exist) | 400, 401, 404, 409 |
| `POST /api/v1/sync` body `SyncRequest` | 200 `SyncResponse` | 400, 401 |

```kotlin
EventDto(id, title, venue, date, opensAt, closesAt, timeZone)
FixerDto(id, displayName, skills: List<String>)
ItemDto(id, eventId, category, description, visitorName, registeredAt)
NewItemDto(eventId, category, description, visitorName, id: String? = null)
RepairSlotDto(id, eventId, fixerId, start, end, itemId: String?)
OutcomeDto(id, itemId, kind, note, loggedAt)
NewOutcomeDto(itemId, kind, note = "", id: String? = null)
StatsDto(eventId, registered, withOutcome, counts: Map<String, Int>,
         repairRatePercent: Int?)
SyncRequest(operations: List<SyncOperationDto>)   // 1..50 operations
SyncResponse(results: List<SyncResultDto>)        // one per operation, in order
SyncResultDto(operationId, status: APPLIED | REJECTED, code?, message?)
ErrorDto(code, message)
```

- **Auth:** every `/api/v1` route needs the header `X-Organizer-Token`, because
  items carry visitors' names. The server accepts any value of the form
  `demo-organizer-<something>` and answers 401 otherwise. In app code use the
  fictional placeholder `YOUR_ORGANIZER_TOKEN`, never a real secret.
- **Time:** `date` is a local date (`"2026-11-21"`), `opensAt` and `closesAt`
  are wall-clock times at the venue (`"10:00"`), `timeZone` is an IANA id
  (`"Europe/London"`). Slot `start`/`end`, `registeredAt` and `loggedAt` are UTC
  instants (`"2026-11-21T10:00:00Z"`). The October event is on British Summer
  Time, so its 10:00 is `09:00:00Z`; the November one is on GMT (`10:00:00Z`).
- **Slots:** the server builds a grid of back-to-back slots for each fixer
  at each event (30 minutes, 45 at `evt-03`) with ids
  `<eventId>.<fixerId>.<n>`, such as `evt-02.fx-01.1`. `itemId` is `null` for
  an open slot.
- **Enums** are strings. `category`: LAMP, SMALL_APPLIANCE, AUDIO, COMPUTER,
  BICYCLE, CLOCK, TOY, CLOTHING, FURNITURE, TOOL. `skills`: ELECTRICAL,
  ELECTRONICS, COMPUTERS, MECHANICAL, BICYCLES, SEWING, WOODWORK, SHARPENING.
  `kind`: FIXED, PARTIAL, NOT_FIXED, ADVICE.
- **Ids:** clients may choose the ids of new items and outcomes (letters,
  digits, `.`, `_`, `-`; at most 64), so that something created offline keeps
  its id. Without an id the server picks the next free `item-NNN` or `out-NNN`.
  Sending the same id with the same data again is a no-op (200); the same id
  with other data is 409 `id_conflict`.
- **Assignment rules** (from `:core:domain`, `checkAssignment`): 409 with code
  `slot_taken` (the slot holds another item), `item_scheduled` (the item has
  another slot), `visitor_busy` (the same visitor has an item at another table
  at an overlapping time) or `wrong_event`. Assigning an item to the slot it
  already holds is a no-op (200).
- **Validation:** `description` 1..200 and `visitorName` 1..80 characters
  after trimming, `note` at most 500; an unknown category or kind is 400.
- **Statistics:** only the latest outcome of each item counts;
  `repairRatePercent` is FIXED + PARTIAL out of FIXED + PARTIAL + NOT_FIXED
  (ADVICE is not a repair attempt), rounded half up, `null` without attempts.
- **Errors:** every error, including unknown routes, has the body
  `ErrorDto {"code","message"}`. Codes: `not_found`, `bad_request`,
  `unauthorized`, `slot_taken`, `item_scheduled`, `visitor_busy`,
  `wrong_event`, `id_conflict`.
- **JSON:** null values are written explicitly (`"itemId":null`); unknown keys
  in request bodies are a 400.

## POST /api/v1/sync: the outbox endpoint

The body is a batch of operations from a client's offline outbox. Each
operation has a client-generated `id`, which is its **idempotency key**, and a
`type`:

```json
{"operations":[
  {"type":"register_item","id":"op-1","item":{ ...ItemDto... }},
  {"type":"assign_slot","id":"op-2","slotId":"evt-02.fx-01.4","itemId":"..."},
  {"type":"log_outcome","id":"op-3","outcome":{ ...OutcomeDto... }}
]}
```

- Operations are applied **in order**, one result each. A refused operation
  does not stop the batch: later ones are still tried.
- A rule violation is a result with `status` `REJECTED` and the same `code` as
  the direct endpoint (`slot_taken`, `not_found`, `bad_request`...), never an
  HTTP error. HTTP errors (400 for a malformed batch, 401) concern the whole
  request; nothing in it was applied.
- **Idempotent:** the server stores the result of every operation id. A replay
  of the same operation returns the stored result, APPLIED or REJECTED, and
  changes nothing, so a client may resend a batch whose response it never
  received. The same id with a different operation is `REJECTED` with code
  `operation_id_reused` (and is not stored).
- `register_item` keeps the client's `registeredAt` and `log_outcome` the
  client's `loggedAt` (the time on the device when it happened); the direct
  endpoints use the server's clock.

## Examples (real output of a fresh server, 8 November 2026)

```text
$ curl -s http://localhost:8086/health
{"status":"ok","events":3}

$ curl -s -i http://localhost:8086/api/v1/events
HTTP/1.1 401 Unauthorized
Content-Length: 79
Content-Type: application/json

{"code":"unauthorized","message":"Missing or invalid X-Organizer-Token header"}

$ curl -s http://localhost:8086/api/v1/events -H 'X-Organizer-Token: demo-organizer-ada'
[{"id":"evt-01","title":"October Repair Café","venue":"Brindle Street Community Hall","date":"2026-10-17","opensAt":"10:00","closesAt":"13:00","timeZone":"Europe/London"},{"id":"evt-02","title":"November Repair Café","venue":"Brindle Street Community Hall","date":"2026-11-21","opensAt":"10:00","closesAt":"13:00","timeZone":"Europe/London"},{"id":"evt-03","title":"Winter Mending Evening","venue":"Brindle Street Library, upstairs room","date":"2026-12-09","opensAt":"18:00","closesAt":"21:00","timeZone":"Europe/London"}]

$ curl -s http://localhost:8086/api/v1/events/evt-02/slots -H 'X-Organizer-Token: demo-organizer-ada' | head -c 300
[{"id":"evt-02.fx-01.1","eventId":"evt-02","fixerId":"fx-01","start":"2026-11-21T10:00:00Z","end":"2026-11-21T10:30:00Z","itemId":"item-011"},{"id":"evt-02.fx-01.2","eventId":"evt-02","fixerId":"fx-01","start":"2026-11-21T10:30:00Z","end":"2026-11-21T11:00:00Z","itemId":null},{"id":"evt-02.fx-01.3",

$ curl -s http://localhost:8086/api/v1/events/evt-01/stats -H 'X-Organizer-Token: demo-organizer-ada'
{"eventId":"evt-01","registered":10,"withOutcome":10,"counts":{"FIXED":5,"PARTIAL":2,"NOT_FIXED":2,"ADVICE":1},"repairRatePercent":78}

$ curl -s -i -X POST http://localhost:8086/api/v1/items -H 'X-Organizer-Token: demo-organizer-ada' -H 'Content-Type: application/json' -d '{"eventId":"evt-02","category":"TOY","description":"Teddy bear lost an eye","visitorName":"Ada Mensah"}'
HTTP/1.1 201 Created
Content-Length: 167
Content-Type: application/json

{"id":"item-015","eventId":"evt-02","category":"TOY","description":"Teddy bear lost an eye","visitorName":"Ada Mensah","registeredAt":"2026-11-08T07:17:21.972099053Z"}

$ curl -s -X POST http://localhost:8086/api/v1/slots/evt-02.fx-07.1/assign -H 'X-Organizer-Token: demo-organizer-ada' -H 'Content-Type: application/json' -d '{"itemId":"item-015"}'
{"id":"evt-02.fx-07.1","eventId":"evt-02","fixerId":"fx-07","start":"2026-11-21T10:00:00Z","end":"2026-11-21T10:30:00Z","itemId":"item-015"}

$ curl -s -i -X POST http://localhost:8086/api/v1/slots/evt-02.fx-03.1/assign -H 'X-Organizer-Token: demo-organizer-ada' -H 'Content-Type: application/json' -d '{"itemId":"item-013"}'
HTTP/1.1 409 Conflict
Content-Length: 80
Content-Type: application/json

{"code":"visitor_busy","message":"Gwen Arbuthnot is busy then (evt-02.fx-01.1)"}

$ curl -s -i -X POST http://localhost:8086/api/v1/outcomes -H 'X-Organizer-Token: demo-organizer-ada' -H 'Content-Type: application/json' -d '{"itemId":"item-015","kind":"FIXED","note":"Sewed on a new button eye"}'
HTTP/1.1 201 Created
Content-Length: 130
Content-Type: application/json

{"id":"out-011","itemId":"item-015","kind":"FIXED","note":"Sewed on a new button eye","loggedAt":"2026-11-08T07:17:22.007551398Z"}

$ curl -s -X POST http://localhost:8086/api/v1/sync -H 'X-Organizer-Token: demo-organizer-ada' -H 'Content-Type: application/json' -d '{"operations":[{"type":"register_item","id":"op-7f1","item":{"id":"9b2e-item","eventId":"evt-02","category":"AUDIO","description":"Speaker crackles","visitorName":"Kofi Asante","registeredAt":"2026-11-21T10:41:00Z"}},{"type":"assign_slot","id":"op-7f2","slotId":"evt-02.fx-01.4","itemId":"9b2e-item"},{"type":"assign_slot","id":"op-7f3","slotId":"evt-02.fx-01.1","itemId":"9b2e-item"}]}'
{"results":[{"operationId":"op-7f1","status":"APPLIED","code":null,"message":null},{"operationId":"op-7f2","status":"APPLIED","code":null,"message":null},{"operationId":"op-7f3","status":"REJECTED","code":"slot_taken","message":"The slot holds item-011"}]}

$ curl -s -X POST http://localhost:8086/api/v1/sync -H 'X-Organizer-Token: demo-organizer-ada' -H 'Content-Type: application/json' -d '{"operations":[{"type":"register_item","id":"op-7f1","item":{"id":"9b2e-item","eventId":"evt-02","category":"AUDIO","description":"Speaker crackles","visitorName":"Kofi Asante","registeredAt":"2026-11-21T10:41:00Z"}},{"type":"assign_slot","id":"op-7f2","slotId":"evt-02.fx-01.4","itemId":"9b2e-item"},{"type":"assign_slot","id":"op-7f3","slotId":"evt-02.fx-01.1","itemId":"9b2e-item"}]}'
{"results":[{"operationId":"op-7f1","status":"APPLIED","code":null,"message":null},{"operationId":"op-7f2","status":"APPLIED","code":null,"message":null},{"operationId":"op-7f3","status":"REJECTED","code":"slot_taken","message":"The slot holds item-011"}]}

$ curl -s -X POST http://localhost:8086/api/v1/sync -H 'X-Organizer-Token: demo-organizer-ada' -H 'Content-Type: application/json' -d '{"operations":[]}'
{"code":"bad_request","message":"A batch has 1 to 50 operations"}
```

The second identical `sync` call is a replay: same answer, no second item.
