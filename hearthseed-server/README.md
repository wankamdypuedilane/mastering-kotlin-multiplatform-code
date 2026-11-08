# hearthseed-server: the local test API of the book

A small Ktor server (JVM, Netty) that plays the role of the
*Larkmoor Commons Seed Library* back end for *Mastering Kotlin Multiplatform*.
Chapter 9 writes it and calls it with the Ktor client; Chapters 12 and 23 reuse it.
Everything in it is fictional: 40 varieties, 54 seed lots, 5 members, 3 loans,
loaded from `src/main/resources/dataset.json` into memory.

## Run

```bash
./gradlew run          # http://127.0.0.1:8085, stop with Ctrl+C
./gradlew test         # testApplication tests of every endpoint
```

The server listens on the loopback interface only (127.0.0.1), because its
token check is not real security. Every restart resets the data (loans made
while it runs are forgotten). Android emulators reach it at `http://10.0.2.2:8085`,
the iOS simulator and desktop/JVM programs at `http://localhost:8085`.

## Contract (stable: later chapters depend on it)

| Method and path | Auth | Success | Errors |
|---|---|---|---|
| `GET /health` | no | 200 `{"status":"ok","varieties":40}` | |
| `GET /api/v1/varieties?query=&family=&page=&pageSize=` | no | 200 `Page<VarietyDto>` | 400 |
| `GET /api/v1/varieties/{id}` | no | 200 `VarietyDto` | 404 |
| `GET /api/v1/varieties/{id}/lots` | no | 200 `List<SeedLotDto>` | 404 |
| `POST /api/v1/loans` body `{"lotId","memberId"}` | token | 201 `LoanDto` | 400, 401, 404, 409 |
| `GET /api/v1/members/{id}/loans` | token | 200 `List<LoanDto>` | 401, 404 |

- **Pagination:** `page` starts at 1 (default 1); `pageSize` is 1..50 (default 20).
  `Page<T>` is `{"items":[...],"page":1,"pageSize":20,"total":40}`; `total` counts
  all matches. A page past the end has an empty `items` list.
- **Filters:** `query` matches a substring of `commonName` or `latinName`, ignoring
  case; `family` matches the botanical family exactly, ignoring case
  (`Solanaceae`, `Fabaceae`, `Cucurbitaceae`, `Brassicaceae`, `Apiaceae`,
  `Asteraceae`, `Amaranthaceae`, `Amaryllidaceae`, `Poaceae`, `Lamiaceae`).
- **Auth:** header `X-Member-Token`. The server accepts any value of the form
  `demo-token-<something>` and answers 401 otherwise. In app code use the
  fictional placeholder `YOUR_MEMBER_TOKEN` and never a real secret.
- **Loans:** a lot can be borrowed only when its status is `AVAILABLE`; the server
  then sets it to `ON_LOAN`. Any other status gives 409. Unknown lot or member: 404.
  `borrowedOn` is today's date and `dueSeason` this year.
- **Errors:** every error, including unknown routes, has the body
  `ErrorDto {"code","message"}` with `code` one of `not_found`, `bad_request`,
  `unauthorized`, `lot_unavailable`.
- **JSON:** null values are written explicitly (`"donorMemberId":null`); dates are
  ISO-8601 strings (`"2026-03-02"`); enums are strings:
  `difficulty` EASY | MEDIUM | HARD, `status` AVAILABLE | ON_LOAN | RETIRED.

DTOs (`src/main/kotlin/hearthseed/server/Dtos.kt`):

```kotlin
VarietyDto(id, commonName, latinName, family, daysToMaturity: Int,
           sowingMonths: List<Int>, difficulty, description)
SeedLotDto(id, varietyId, harvestYear: Int, grams: Int, germinationRate: Int,
           donorMemberId: String?, status)
LoanDto(id, lotId, memberId, borrowedOn, dueSeason: Int, returnedGrams: Int?)
```

Keys are camelCase and identical to the domain field names of `docs/PROJECTS.md`;
ids are strings. Clients may read `difficulty` and `status` as enums
(`code/ch09-ktor`) or as plain strings (`code/ch10-repositories`): the JSON is the same.

## Examples (real output of a fresh server, 8 November 2026)

```text
$ curl -s http://localhost:8085/health
{"status":"ok","varieties":40}

$ curl -s 'http://localhost:8085/api/v1/varieties?family=Brassicaceae&pageSize=2&page=2'
{"items":[{"id":"var-022","commonName":"Lattice broccoli","latinName":"Brassica oleracea","family":"Brassicaceae","daysToMaturity":120,"sowingMonths":[4,5],"difficulty":"MEDIUM","description":"Purple sprouting broccoli that overwinters and crops in the hungry gap of early spring."},{"id":"var-023","commonName":"Firebrand radish","latinName":"Raphanus sativus","family":"Brassicaceae","daysToMaturity":28,"sowingMonths":[3,4,5,6,7,8,9],"difficulty":"EASY","description":"Round scarlet radish ready in four weeks. Sow a short row every fortnight."}],"page":2,"pageSize":2,"total":6}

$ curl -s http://localhost:8085/api/v1/varieties/var-010
{"id":"var-010","commonName":"Larkmoor Pea","latinName":"Pisum sativum","family":"Fabaceae","daysToMaturity":60,"sowingMonths":[3,4],"difficulty":"EASY","description":"A tall shelling pea from the founding members' gardens. Sow early and give it twiggy supports."}

$ curl -s http://localhost:8085/api/v1/varieties/var-999
{"code":"not_found","message":"Variety var-999 not found"}

$ curl -s http://localhost:8085/api/v1/varieties/var-004/lots
[{"id":"lot-005","varietyId":"var-004","harvestYear":2023,"grams":50,"germinationRate":88,"donorMemberId":"mem-004","status":"ON_LOAN"},{"id":"lot-006","varietyId":"var-004","harvestYear":2024,"grams":8,"germinationRate":70,"donorMemberId":null,"status":"AVAILABLE"}]

$ curl -s 'http://localhost:8085/api/v1/varieties?pageSize=500'
{"code":"bad_request","message":"pageSize must be in 1..50"}

$ curl -s -i -X POST http://localhost:8085/api/v1/loans -H 'Content-Type: application/json' -d '{"lotId":"lot-006","memberId":"mem-003"}'
HTTP/1.1 401 Unauthorized
Content-Length: 76
Content-Type: application/json

{"code":"unauthorized","message":"Missing or invalid X-Member-Token header"}

$ curl -s -i -X POST http://localhost:8085/api/v1/loans -H 'X-Member-Token: demo-token-cleo' -H 'Content-Type: application/json' -d '{"lotId":"lot-006","memberId":"mem-003"}'
HTTP/1.1 201 Created
Content-Length: 120
Content-Type: application/json

{"id":"loan-004","lotId":"lot-006","memberId":"mem-003","borrowedOn":"2026-11-08","dueSeason":2026,"returnedGrams":null}

$ curl -s -i -X POST http://localhost:8085/api/v1/loans -H 'X-Member-Token: demo-token-cleo' -H 'Content-Type: application/json' -d '{"lotId":"lot-006","memberId":"mem-003"}'
HTTP/1.1 409 Conflict
Content-Length: 76
Content-Type: application/json

{"code":"lot_unavailable","message":"Lot lot-006 is ON_LOAN, not AVAILABLE"}

$ curl -s http://localhost:8085/api/v1/members/mem-003/loans -H 'X-Member-Token: demo-token-cleo'
[{"id":"loan-004","lotId":"lot-006","memberId":"mem-003","borrowedOn":"2026-11-08","dueSeason":2026,"returnedGrams":null}]
```

The second POST fails with 409 because the first one put `lot-006` on loan.
