# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project overview

**Kuna Ayuda** (user-facing display name; the repo, package `com.jyodroid.kunasismoayuda`, modules and
DB names stay `kunasismoayuda`/`kuna-sismo-ayuda`) is a disaster-relief resource network, starting with
earthquakes and prioritizing affected regions. It is **multi-country** — **Colombia (CO)**, **Indonesia (ID)**, **Spain (ES)**, **Italy (IT)** and **Peru (PE)** —
with a first-run country picker (persisted) and a country switcher on the Overview tab. It is a Kotlin Multiplatform monorepo:

- **Client** — Compose Multiplatform app targeting **Android, iOS, and Desktop (JVM)**. Shows a
  live, region-prioritized earthquake feed + an active-**wildfire** feed (second hazard) + a MapLibre
  help-points map for the **selected country**;
  localized ES (default) / EN / **Bahasa Indonesia** / **Italian** (device-locale driven).
- **Backend** (`:server`) — a **Ktor** server (JVM) that aggregates earthquake data from upstream
  sources (SGC primary for CO, USGS fallback/primary elsewhere), normalizes it, and owns moderated
  data (shelters/acopios, help & SOS requests) plus push fan-out in later milestones.

**Multi-country design:** the client sends `?country=CO|ID|ES|IT|PE` on `GET /api/quakes`, `/api/shelters`
and `/api/board`; **the server owns the quake bounding box** (`upstream/CountryBBoxes`) and filters
shelters/board by a `country` column. **The client owns the per-country region lists** (major cities)
in `core/domain` (`ColombiaRegions`/`IndonesiaRegions`/`SpainRegions`/`ItalyRegions`/`PeruRegions`, dispatched by `CountryRegions.of`)
used for affected-place logic + map centering, plus the `Country` enum (code, localized names, flag,
map centroid/zoom). The chosen country persists via `core/data` `settings/CountryStore` (okio JSON,
per-platform path in `settings/SettingsStorage.*`; in-memory fallback). The **Guide** (emergency
channels + the mental-health line in the tips) and the **SOS "call" number** are country-specific too,
driven by `core/domain` `CountryEmergency` (verified per-country numbers — CO 123/119/132/144/192;
ID 112/110/113/119/115/129 + SEJIWA; ES 112/061/091/062/080/024 + Cruz Roja; IT 112/118/115/113/1530
+ Telefono Amico; PE 105/116/106/115 + Línea 113 opción 5); Colombia-only community
tips (IG handles, local animal foundations) are hidden for other countries. SOS stays **global** (emergency,
responder-facing) — it has a `country` column but the responder view isn't scoped yet. GDACS/ReliefWeb
**ingestion is still Colombia-only** (feeds the disasters tables, not the quake feed).

The client's build structure and convention plugins were ported from the `chatApp` KMP template; the
`:server` config/structure mirrors the `herrajes-el-paisa` Ktor backend.

## Commands

```shell
# Backend
./gradlew :server:run                 # run the Ktor server on :8080 (also serves the landing at /)
./gradlew :server:shadowJar           # fat jar -> server/build/libs/server-all.jar
./gradlew :server:stage               # Heroku deploy hook (builds the fat jar)
./gradlew :server:test                # server tests (Testcontainers -> needs Docker)
./gradlew :server:buildLanding        # rebuild the Vite landing -> server/src/main/resources/web (needs Node)

# Client
./gradlew :composeApp:run             # run the Desktop (JVM) app
./gradlew :composeApp:assembleDebug   # build Android debug APK
./gradlew build                       # full build across all targets

# Library modules use the Android KMP library test layout:
./gradlew :core:domain:jvmTest        # quake use-case tests live here now (core/domain/usecase)
./gradlew :core:data:testAndroidHostTest
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode (the Compose framework is named `ComposeApp`).

### Local database setup (backend)
The server needs Postgres only when a DB is configured (`DATABASE_URL_KUNA` or `DB_PASSWORD_KUNA`;
otherwise it runs and `/api/*` return empty). Config comes from **project-scoped** env vars
(`DbEnvParser`) so a generic `DATABASE_URL` exported for another project can't hijack it:
- Database `kuna_sismo_db`, role `kuna_user` (local dev password `kunapass`). One-time create:
  `psql -d postgres -c "CREATE ROLE kuna_user LOGIN PASSWORD 'kunapass';"` then
  `psql -d postgres -c "CREATE DATABASE kuna_sismo_db OWNER kuna_user;"`
- `DATABASE_URL_KUNA=postgres://kuna_user:kunapass@localhost:5432/kuna_sismo_db` (Heroku-style URL)
  takes precedence; else it falls back to the local `kuna_sismo_db`/`kuna_user` with `DB_PASSWORD_KUNA`.
  Flyway applies `V1–V3` on first run. Set both in `~/.zshrc` so gutter "run main" runs get them.
- The committed **`.run/Server.run.xml`** ("Server" config) also sets `DATABASE_URL_KUNA` (and
  `FIRMS_MAP_KEY`) explicitly, so they work from the run dropdown even before a shell/IDE restart — a
  Dock-launched IDE does NOT inherit `~/.zshrc`, so any env the local `:8080` server needs (e.g. the
  FIRMS key for `/api/fires`) must be set here too, not only in the shell profile.
- **Migration discipline:** never edit an already-applied `V*.sql` (Flyway checksum mismatch) — add a
  new `V{n}__*.sql`. In early dev, recreating `kuna_sismo_db` is fine. (Migrations are at `V15`:
  `V10__multi_country.sql` adds a `country` column to `shelters`/`resource_posts`/`sos_reports`
  (default `'CO'`, indexed); `V11__seed_id_es_points.sql` seeds real official ID/ES help points;
  `V12__search_and_photos.sql` adds `photos` (image `bytea`) + `search_reports` (Lost & Found pets +
  people) tables; `V13__sos_handled.sql` adds `handled_at`/`handled_by` to `sos_reports` for the
  responder attended/notified lifecycle; `V14__board_contact.sql` makes `resource_posts.contact_phone`
  nullable and adds `contact_email` (opt-in contact on board posts); `V15__board_owner.sql` adds
  `resource_posts.owner_secret` for device-gated resolve; `V16__seed_it_points.sql` seeds real official
  Italian help points — Croce Rossa Italiana HQ + committees, Protezione Civile, Vigili del Fuoco — in
  Rome and the seismic Apennine/southern cities, mirroring `core/domain ItalyRegions.kt`;
  `V17__factcheck_and_usage.sql` adds `resource_posts.fact_check` (Google Fact Check note) + the
  `api_usage` (monthly spend-cap counter) and `classify_cache` (memoized classify+fact-check result)
  tables; `V18__seed_pe_points.sql` seeds real official **Peruvian** help points — INDECI, Cruz Roja
  Peruana (national + filiales), CGBVP (bomberos) — in Lima and the seismic capitals, mirroring
  `core/domain PeruRegions.kt`.)
- **Flyway + fat jar (FIXED):** the shaded fat jar (`:server:shadowJar` → `java -jar`, the
  `:server:stage`/Heroku path) previously applied **0 migrations** — Flyway 11 registers its SQL
  resolvers + the config extensions that define the `V__`/`R__`/`U__` naming via
  `META-INF/services/org.flywaydb.core.extensibility.Plugin`, and flyway-core (27 entries) +
  flyway-database-postgresql (3) each ship one. Shadow's service-file merge collapsed them to
  postgres-only, so `ResourceNameParser` had no prefixes and rejected every migration ("N SQL migrations
  … did not follow the filename convention"). Fix: we ship the **fully-merged list** at
  `server/src/main/resources/META-INF/services/org.flywaydb.core.extensibility.Plugin` (30 entries) and
  `shadowJar { mergeServiceFiles { exclude("org.flywaydb.core.extensibility.Plugin") } }` so that file is
  packaged verbatim, not overwritten. `ServiceLoader` dedupes provider names, so local `:server:run`
  (which also sees the dependency copies) is unaffected. Verified: the fat jar applies V1–V13 on a fresh
  DB. **If the Flyway version changes, regenerate that file** (union of both jars' Plugin service files).
  `FlywayInitializer` also passes its own classloader explicitly.
- **`ANTHROPIC_API_KEY`** (optional): enables the board's paste-and-classify endpoint. Without it,
  `POST /api/board/classify` returns a clean `503`; the rest of the server runs normally.
- **`FIRMS_MAP_KEY`** (optional): a free NASA FIRMS Area-API key enabling the dense active-fire **point**
  feed on `GET /api/fires`. Without it FIRMS is disabled and the endpoint falls back to keyless GDACS
  wildfire events (coarser); the server runs normally either way.
- **`FACT_CHECK_API_KEY`** (optional): a free Google **Fact Check Tools API** key. When set, the board
  paste-and-classify flow also searches the pasted text for matching fact-checks and attaches a
  moderator-facing note (`resource_posts.fact_check`, shown in the moderation queue) — a *signal*, never
  an auto-reject. Unset ⇒ no fact-check note; classification still works.
- **Spend caps** (`services/UsageLimiter`, this is a personally-funded project): **`ANTHROPIC_MONTHLY_LIMIT`**
  and **`FACTCHECK_MONTHLY_LIMIT`** are per-feature **monthly** call caps. Unset/blank ⇒ unlimited; `0` ⇒
  disabled; `N>0` ⇒ N calls/UTC-month. When the cap is hit, `POST /api/board/classify` returns **429**
  and stops calling out (checked BEFORE the paid call; the Anthropic count records only on **success** so
  a bad key can't burn the budget). Counts persist in `api_usage` (survives restarts; in-memory fallback
  when no DB). A **`classify_cache`** table memoizes the classify+fact-check result by a SHA-256 of the
  normalized pasted text, so the same viral post pasted again is served from cache with **no** paid call.
- **Moderator auth** — only moderators ever authenticate; every regular user (quakes, shelters, SOS,
  board posting/classify) stays anonymous. `JWT_SECRET` signs/verifies tokens (defaults to an insecure
  dev secret if unset — set it in prod). `ADMIN_EMAIL` + `ADMIN_PASSWORD` seed the first moderator into
  `admin_users` on startup (idempotent, bcrypt-hashed via jbcrypt); unset ⇒ no moderator exists and
  login returns `401`. `POST /api/auth/login` (public, rate-limited) is the **only** token issuer — it
  mints a 12h JWT carrying the account's `role` claim (login also returns the role so the client can
  gate role-specific UI). **Two roles** (`domain/models/Roles`): `ADMIN` moderates content;
  `SUPERADMIN` additionally manages other admin accounts. The env-seeded owner is the SUPERADMIN;
  `requireAdmin()` (ADMIN or SUPERADMIN) guards the board admin routes, `requireSuperAdmin()` guards
  admin-account management. `GET/POST/DELETE /api/admins` (**super-admin only**, `AdminService` +
  `AdminRoutes`) list/create/delete moderators — created accounts are always plain ADMIN (the API can't
  mint another superadmin), and delete refuses self-deletion and the SUPERADMIN owner.
- **Oversight console ("monitor the monitors")** — server + a web console, **apps untouched**. Flyway
  **V19** adds `admin_audit` (append-only: actor/action/entity/`before_json`/`after_json`/ip/timestamps/
  `reverted_at`+`by`) and `admin_users.enabled`. `services/AuditService` (+ `AuditRepository`,
  `AuditSnapshots` @Serializable DTOs) **records every moderator mutation** — shelters create/update/
  delete, board approve/reject, SOS handle/reopen/delete, search delete, admin create/delete/disable/
  enable, password change/reset, login success/failure — from the **route layer** via `routes/RouteSecurity.kt`
  `actor()` (callerEmail + role + `X-Forwarded-For` IP). **Revert** (single + `revert-all` by a moderator,
  **conflict-aware**: skips an entity a *different* mod changed later) restores from the snapshots (repo
  additions: `Shelter.find/reactivate`, `Board.restore`, `Search.find/reopen`, `Sos.find`; SOS/search
  deletes are re-created from the snapshot). **Disable** neutralises a rogue mod instantly: `requireAdmin`/
  `requireSuperAdmin` also verify `enabled` (one DB lookup), so a disabled account's still-valid 12h JWT
  stops working. **Passwords:** self-service `POST /api/auth/password` (current+new, any moderator) +
  super-admin `POST /api/admins/{id}/password` (reset). **Super-admin read/act API** (all
  `requireSuperAdmin`): `GET /api/audit` (filters actor/action/entity/reverted, paginated),
  `GET /api/audit/moderators` (per-mod activity), `POST /api/audit/{id}/revert`,
  `POST /api/audit/revert-all?moderator=`. `AuditServiceTest` (mockk) covers revert inverses + conflict
  skip. The **web console** lives at **`console/`** (React 19 + Vite + Tailwind, mirroring the herrajes
  `main-landing/frontend` stack — the vanilla-TS `landing/` is unchanged); Vite `base:'/console/'`, served
  by `staticResources("/console","console")` (registered before the `/` catch-all; non-`/api` so ungated),
  built via **`:server:buildConsole`** (manual, like `:server:buildLanding`) into
  `server/src/main/resources/console/`. Login → JWT in `sessionStorage`; `src/api.ts` attaches `Bearer` +
  `X-App-Key` (same baked deterrent as the app) and signs out on 401. **Role-gated:** any moderator sees
  only "Change my password"; SUPERADMIN sees Audit (filter table + before/after diff + revert), Moderators
  (activity, disable/enable, reset password, revert-all), and Moderation (board approve/reject, shelters,
  SOS). The landing footer has a discreet **"Moderadores" → /console** link (regular mods use it for the
  password change too). **No app-side moderator self-service** (deliberate). Deferred: CSV export, alerts, 2FA.
- **App-only gate** (`config/AppGate.kt`) — optional `APP_CLIENT_KEY` env. When set, every `/api/**`
  request must carry an `X-App-Key` header matching it (constant-time compare; `OPTIONS`/non-`/api`
  exempt); the KMP client sends it automatically from `core:data` `ServerConfig.APP_CLIENT_KEY` via
  `HttpClientFactory`'s `DefaultRequest`. Unset ⇒ gate disabled (dev/open mode), like the other
  env-gated toggles. It's a **deterrent** (the key ships in the app binary and is extractable), not
  identity proof — set it in prod (to the same value baked in the client) to shut out
  scanners/bots/browser callers; real "only our app" enforcement (attestation) is deferred.
  CORS (`config/Cors.kt`) still only constrains browsers: `ALLOWED_ORIGIN` locks it in prod, else
  `anyHost()` for dev. **Prod env checklist:** `JWT_SECRET`, `ALLOWED_ORIGIN`, strong `ADMIN_PASSWORD`,
  and (for the app gate) `APP_CLIENT_KEY`. **Enforced by `config/SecurityPreflight.kt`**
  (`configureSecurityPreflight()`, runs **first** in `module()`): in production (`APP_ENV`/`KTOR_ENV` =
  `production`|`prod`) a missing/default `JWT_SECRET` — the one catastrophic gap, since it makes
  ADMIN/SUPERADMIN JWTs forgeable — **aborts boot** (`IllegalStateException`, exit 1); softer gaps
  (unset `ALLOWED_ORIGIN`/`APP_CLIENT_KEY`, absent or <12-char `ADMIN_PASSWORD`) log `[security]`
  warnings in every env but never block. Dev (env unset) only ever warns, so nothing changes locally.

## Architecture

### Module graph
- `:server` — Ktor (Netty) server, plain `kotlin("jvm")`. **Self-contained**: owns its own DTOs and
  does not depend on the KMP `core:*` modules. Layered like herrajes-el-paisa: `config/`, `di/`
  (Koin), `serializer/`, `error/` (StatusPages), `routes/` (+ `dto/`), `upstream/` (SGC/USGS clients),
  `services/`, `domain/` + `infrastructure/` (Exposed tables/repos). DB (Exposed + Postgres + Flyway)
  activates only when `DATABASE_URL`/`DB_PASSWORD` is set; without it the server still runs and
  `/api/shelters` returns an empty list (`DatabaseFactory.initialized` gates the repo).
  Endpoints: `POST /api/auth/login` (public, rate-limited — moderator sign-in, the only JWT issuer;
  `AuthService` + `AdminUserRepository`); `GET/POST/DELETE /api/admins` (**super-admin only** —
  moderator-account management, `AdminService` + `AdminRoutes`); `GET /api/quakes` (public, `?country=CO|ID|ES` picks the
  bbox via `CountryBBoxes`; SGC primary only for CO, else USGS); `GET /api/fires` (public,
  `?country=CO|ID|ES|IT|PE` — active wildfires, FIRMS primary when `FIRMS_MAP_KEY` set else GDACS fallback);
  `GET /api/shelters` (public,
  moderated, `?country=` filtered) + `POST/PUT/DELETE /api/shelters` (**admin-only**, anti-fraud; POST
  carries `country`; `PUT /{id}` edits an active point → 200 / 404); `GET /api/board` (public, `?country=` +
  filters kind/region/type — **ACTIVE only**) + `POST /api/board` (public but **rate-limited** +
  validated; **contact is opt-in** — `contactPhone`/`contactEmail`/`contactName` all optional, only
  `region`/`kind`/`type` required, email format-checked when present) + the **two-step paste-and-classify
  flow**: `POST /api/board/classify` (public, rate-limited — pastes free text, **`ai/AnthropicClient`**
  calls Claude via the Ktor client with structured outputs, and returns a **`ClassifyPreviewResponse`
  for the poster to review — nothing is persisted yet**) then `POST /api/board/classify/confirm` (the
  poster confirmed → queues a **PENDING** entry; the classify work is served from `classify_cache`, so
  confirm makes no new paid call). Both share `classifyToEntry` in `ResourceBoardService` (link-only
  guard **before** any paid call + empty-extraction guard after → **422** `UnclassifiableTextException`
  with "paste the text, not a link" guidance, so a bare URL/screenshot never becomes an empty post) and
  the same error mapping (`classifyGuarded`). Then `POST /api/board/{id}/resolve`
  (**public, device-gated** — the creating device closes its own post by presenting the `owner_secret`
  the server issued at creation and returned once in the create response; no auth, the secret IS the
  identity; constant-time compare; RESOLVED→204 / not-owner→403 / missing→404) + `GET /api/board/pending`
  + `POST /api/board/{id}/approve` + `DELETE /api/board/{id}` (all **admin**, the classified-content
  moderation flow — approve→ACTIVE, **delete/resolve→CLOSED and scrubs the post's
  contact_phone/email/name + owner_secret** so contact is public only while a post is live); `POST /api/sos` (public,
  **not** rate-limited — never block someone in danger; accepts SOS or SAFE) + the **admin responder
  lifecycle**: `GET /api/sos` (`?status=SOS|SAFE` filters — omit/`ALL` returns both, unknown falls back
  to both rather than erroring; `?archived=false` default = pending, `true` = archived, `all` = both) +
  `GET /api/sos/stats` (pending-vs-handled counts by kind) + `POST /api/sos/{id}/handle` (archive as
  attended/notified, stamps the moderator email) + `POST /api/sos/{id}/reopen` (restore to active) +
  `DELETE /api/sos/{id}` (permanent); `GET /api/search` (public, **Lost & Found / reunification** — pets +
  people, `?country=&subject=PET|PERSON&state=LOST|FOUND`, ACTIVE only) + `POST /api/search` (public,
  **rate-limited** + validated, **direct-post** — no moderation) + `DELETE /api/search/{id}` (**admin**,
  anti-abuse); `POST /api/photos` (public, rate-limited — multipart image, allowlisted JPEG/PNG/WebP,
  ≤3 MB, stored as Postgres `bytea`) + `GET /api/photos/{id}` (public, serves the bytes) — the images
  for search reports; `GET /api/disasters` + `GET /api/disaster-reports` (public reads of
  the **ingested feeds** — each item carries `fetchedAt` epoch-millis for freshness). **Ingestion job**
  (`services/DisasterIngestionService` + `config/DisasterIngestion.kt`): on startup and every 30 min it
  pulls **GDACS** (`upstream/GdacsSource` — SEARCH endpoint, EQ, 30-day window, filtered to Colombia by
  ISO3/bbox; no key) and **ReliefWeb** (`upstream/ReliefWebSource` — v2 API, "Colombia earthquake"),
  UPSERTs by `(source, external_id)` so re-fetches update in place. Each source is isolated (one
  failing doesn't abort the other). **ReliefWeb v2 needs a ReliefWeb-approved `appname`** (set
  `RELIEFWEB_APPNAME`; arbitrary names get 403 → 0 reports, gracefully); GDACS works with no key.
  Help points stay on the moderated-submission path (never from these feeds). **Expiry sweep**
  (`services/ExpiryService` + `config/BoardExpiry.kt`): on startup and every 24 h it auto-closes ACTIVE
  aid-board posts + Lost & Found search reports older than `ExpiryService.EXPIRY_DAYS` (30) — bulk
  `expireOlderThan(cutoff)` on both repos, reusing each `close()` semantics (board posts also get
  contact + `owner_secret` scrubbed; search reports flip to CLOSED). Keeps the feeds fresh; the create
  forms carry a 30-day auto-archive disclaimer (`expiry_notice_30d`). Change the window in one place
  (`EXPIRY_DAYS`). Shelters/help points and SOS are **not** swept (official/emergency data). Flyway `V1__init.sql` (shelters), `V2__resource_board.sql`
  (resource_posts), `V3__sos.sql` (sos_reports), `V4__board_classify.sql` (adds `source`/`raw_text`/
  PENDING status), `V5__admin_users.sql` (moderator accounts), `V6__seed_official_points.sql`
  (replaces the V1 placeholder shelters with **real official institutions** — UNGRD, IDIGER, Cruz Roja
  national + Bogotá seccional, Defensa Civil, DAGRD Medellín — geocoded from their public portals),
  `V7__seed_regional_points.sql` (one **Cruz Roja seccional per department capital**, for all 16
  cities in `core/domain` `ColombiaRegions`, at those city coordinates — so wherever a quake hits, the
  affected capital has an official point; exact street flagged "por confirmar" for a moderator), and
  `V8__seed_affected_choco_valle.sql` (event-specific points for the Aug 2026 Chocó/Valle quake in the
  **non-capital affected municipalities** V7 doesn't cover — San José del Palmar, Quibdó, Condoto,
  Buenaventura — seeded honestly as coordinated humanitarian ops rather than walk-in donation centres,
  plus a verified Cruz Roja Cali street address), and `V9__disasters_reports.sql` (the `disasters` +
  `disaster_reports` tables for the ingestion job, deduped by a `(source, external_id)` unique key with
  a `fetched_at` staleness stamp), and `V10`/`V11` (multi-country — see the migrations note above) own
  the schema.
  `configureRateLimit()` (`config/RateLimit.kt`) installs **`XForwardedHeaders`** (so the limiter keys
  on the real client IP behind Heroku's proxy via `X-Forwarded-For`, not the router) and registers two
  **per-IP** limiters (`requestKey { client IP }`, blank → shared `"unknown"` bucket): **`board`** (5
  writes / 60s — board + search + photo POSTs) and a separate **`login`** (10 attempts / 5 min — the
  moderator login, its own bucket so board spam and credential brute-forcing can't starve each other).
  Before this, `board` was a single global bucket shared with login, so one abuser throttled everyone.
  **Request-body cap** (`config/RequestSizeLimit.kt`, `configureRequestSizeLimit()`): rejects
  `/api/` POST/PUT bodies over **128 KB** with **413** by `Content-Length`, before `receive()` reads
  them into memory (all JSON writes here are tiny text). `/api/photos` is exempt (multipart, self-caps
  at 3 MB in `PhotoRoutes`); SOS is capped too but a real SOS is <1 KB so it's never blocked. Chunked
  bodies with no `Content-Length` slip past the header check (accepted limitation).
  **Security response headers** (`config/SecurityHeaders.kt`, `configureSecurityHeaders()`): every reply
  carries `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` + CSP `frame-ancestors 'none'`
  (anti-clickjacking; CSP scoped to framing only so it won't block the future landing page's assets),
  `Referrer-Policy: no-referrer`, plus `Strict-Transport-Security` **in production only** (`isProductionEnvironment()`
  in `config/Env.kt` — `APP_ENV`/`KTOR_ENV` = `production`|`prod`; shared with the preflight). Cheap
  defence-in-depth for the browser attack surface (native apps ignore them).
- `:composeApp` — the app entry points (Android/iOS/Desktop) and the quake/help UI + navigation +
  DI wiring. Depends on all `core:*` and `feature:map`. **Country gate** (`ui/settings`):
  `AppSettingsViewModel` exposes `CountryState` (Loading / NeedsSelection / Selected) from
  `CountryStore`; `App()` shows `CountryPickerScreen` on first launch, then `AppContent(country)` whose
  `LaunchedEffect(country)` reloads the Quakes/Shelters/Board VMs (each has `setCountry`) — the Overview
  tab has a `CountrySelector` dropdown to switch. **Moderation UI** (`ui/auth`,
  `ui/moderation`, `ui/admin`): a discreet "Moderación" entry at the bottom of the Guide screen opens the
  `moderation` route, which shows `LoginScreen` until there's a session and the `ModerationScreen`
  (pending queue → approve/reject, showing each classified post's original `rawText`) after. The
  top-bar SOS action is replaced by "Sign out" on that route. `AuthViewModel.session` (null for every
  normal user) is what gates it; regular users never see a login wall. **Super-admin console**
  (`ui/admin` — `AdminViewModel` + `AdminManagementScreen`): when the session's `role` is `SUPERADMIN`,
  `ModerationScreen` shows a "Manage moderators" entry that pushes the `admins` route (list moderator
  accounts, add one via email+password, delete with a confirm dialog) over `AdminRepository` →
  `/api/admins`; the SUPERADMIN owner row has no delete affordance. Plain ADMINs never see the entry.
  **SOS responder view** (`ui/sos` — `SosResponderViewModel` + `SosResponderScreen`): `ModerationScreen`
  shows a "Ver alertas SOS" entry (any logged-in moderator, ADMIN or SUPERADMIN) that pushes the
  `sos_responder` route over `SosRepository.listActive(status)` → `GET /api/sos`. It lists submitted
  reports newest-first with a filter (SOS / A salvo / Todos), SOS cards in the error container + SAFE in
  a calm surface (status also named in text — never colour alone), a "Ver en el mapa" button opening the
  coords via `LocalUriHandler`, and a call button. **Attended/notified lifecycle:** a top **stats row**
  (SOS pendientes / A salvo pendientes / Atendidos, the pending-SOS cell emphasised when >0), an
  **Activos ↔ Archivados** segmented toggle, and per-card actions — active cards show "Atendido" (SOS) /
  "Notificado" (SAFE) which archive via `POST /{id}/handle`; archived cards show "Restaurar" (`reopen`)
  and "Eliminar" (`DELETE`, behind a confirm dialog) and display who handled it. Soft-archive is the
  default (reversible, audit trail); hard delete is the escape hatch for spam/false alarms.
  **Add help point** (`ui/shelters` — `ShelterAdminViewModel` + `ShelterCreateScreen`): `ModerationScreen`
  shows an "Agregar punto de ayuda" entry (any logged-in moderator) → `shelter_create` route, a form
  (name, type chips, address, optional accepts/hours/phone) with a **map-tap location picker**
  (`DisasterMap` `onMapTap` drops the pin, coloured by the chosen type) POSTing to the existing
  `POST /api/shelters` (admin) via `ShelterRepository.create`; on success the shelters list/map reload.
  This is the in-app way to publish official help points (previously only Flyway seeds / raw API).
- `:core:domain` — models (`Quake`, `Region`, `ColombiaRegions`/`IndonesiaRegions`/`SpainRegions` +
  `CountryRegions.of`, `Country` enum), `QuakeRepository` interface, `Geo` util.
- `:core:data` — Ktor client (`QuakeApi`), DTO + mapper, `QuakeRepositoryImpl`, per-platform base URL.
  Applies the `kotlin.serialization` plugin (required so the `@Serializable` DTOs actually get
  generated serializers at runtime). `offline/` holds the **SOS outbox**: `SosOutbox` persists every
  SOS/SAFE report to an okio JSON file (atomic temp-swap; per-platform dir via `expect
  outboxFilePath()`, in-memory fallback when unavailable) *before* the network call, returns
  `SosSendResult.SENT`/`QUEUED`, and a backoff loop (15s→5m) retries queued reports until delivered.
  `SosRepository.pending` exposes the queue depth; permanent 4xx rejections are dropped, transient
  failures retried. Covered by `SosOutboxTest` (FakeFileSystem + Ktor MockEngine). `auth/SessionManager`
  holds the moderator's bearer token **in memory only** (never on disk — a token doesn't survive an app
  kill); `AuthApi`/`AuthRepositoryImpl` log in and set it, and `ResourceBoardRepositoryImpl` reads it
  (`requireToken()`, throws `NotAuthenticatedException` if absent) to attach `bearerAuth` on the
  moderator-only `listPending`/`approve`/`reject` calls. **Auto-logout on token expiry:** the shared
  Ktor client (`HttpClientFactory.create(onUnauthorized)`) installs an `HttpResponseValidator` that,
  on a **401 to a request carrying an `Authorization` header** (expired/revoked JWT — the 12h token
  ran out), calls `SessionManager.expire()` (DI wires `onUnauthorized = { get<SessionManager>().expire() }`).
  `expire()` clears the token/session and raises `sessionExpired`, so the UI drops straight back to
  `LoginScreen` (which shows a "session expired" hint via `AuthViewModel.sessionExpired`) and every
  pushed moderator route pops (they already guard `if (session == null) popBackStack()`). Gating on the
  Authorization header means a public-endpoint 401 (bad login creds, app-gate) never nukes a session.
  Covered by `HttpClientAuthTest` (MockEngine).
- `:core:location` — `expect fun createLocationProvider(): LocationProvider` returning
  `Granted(lat,lon)` / `PermissionRequired` / `Unavailable`. Android actual uses `LocationManager`
  (context set via `AndroidLocationContext` in `MainActivity`, which also requests the runtime
  permission). iOS actual uses **CoreLocation** (`CLLocationManager`): returns a cached fix or a
  one-shot `requestLocation()` (10s timeout so SOS never blocks), prompts via
  `requestWhenInUseAuthorization()` — **requires `NSLocationWhenInUseUsageDescription` in
  `iosApp/iosApp/Info.plist`** (already added). Desktop (JVM) returns `Unavailable` (no GPS).
- `:core:presentation`, `:core:designsystem` — shared scaffolding (mostly empty in M1).
- `:core:media` — platform image capture (a capability wrapper like `core:location`, but Compose):
  `@Composable expect fun rememberImagePicker(onResult)` + `ImagePicker`/`PickedImage`/`sniffMime`.
  Android = system photo-picker + `TakePicturePreview` camera; iOS = `UIImagePickerController`;
  Desktop (JVM) = file chooser, no camera (all downscale to ~1280 px JPEG). Each target has its own
  actual, so the default hierarchy wires the iOS leaves to `iosMain` automatically (no `mobileMain`).
  Used by `ui/search`.
- `:core:beacon` — the **offline emergency light+sound beacon** (a capability wrapper like
  `core:location`): `expect fun createBeaconDevice(): BeaconDevice` (torch + tone primitives) with the
  shared SOS-morse loop/auto-stop in commonMain `EmergencyBeacon`. Android needs `AndroidBeaconContext`
  set in MainActivity. Used by `ui/sos`. See the **Offline emergency beacon** note under Roadmap for the
  full design + rationale.
- **Quake use-cases live in `:core:domain`** (`core/domain/usecase/`: `GetQuakesUseCase`,
  `PrioritizeByRegionUseCase`, `IdentifyAftershocksUseCase`) — they're pure domain logic over
  `core:domain` models, so they belong in the domain layer, not a separate module. (The old
  half-module `:feature:quakes:domain` was folded in 2026-08-16 — see the architecture note below.)
- `:feature:map` — `expect/actual DisasterMap(markers, onMarkerTap, focusLat, focusLon, …, onMapTap)`
  plotting a list of `MapMarker`s coloured by `MarkerKind` (QUAKE / ACOPIO / ALBERGUE / SALUD / AGUA /
  OTRO — one MapLibre `CircleLayer` per kind, fixed count for stable composition). Optional `onMapTap`
  (lat, lon) turns the map into a **location picker** (wired to MapLibre's `onMapClick`; null = inert
  taps) — used by the moderator "add help point" form. Android + iOS share a
  `mobileMain` source set using **MapLibre Compose** (`org.maplibre.compose:maplibre-compose`);
  desktop (`jvmMain`) is a **stop-gap** (MapLibre desktop needs a Java 25 toolchain, deferred): it
  shows explanatory text plus an **"Abrir el mapa en el navegador"** button that opens the focus point
  (or the first help point) on OpenStreetMap via `java.awt.Desktop.browse` — so desktop isn't a dead
  end, though tap-to-drop-a-pin (the moderator location picker) still isn't possible there.
  **Source-set wiring note:** the iOS *leaf* source sets (`iosArm64Main`/`iosSimulatorArm64Main`)
  `dependsOn(mobileMain)` **directly** — not via the intermediate `iosMain`. Manual `dependsOn` edges
  disable the default hierarchy template, which otherwise links the leaves to `iosMain`, so routing
  through `iosMain` silently drops `mobileMain` (the actual + MapLibre dep) from the iOS compilation.
  iOS still needs a one-time Xcode SPM step for the MapLibre **native** framework at app-link time —
  see `feature/map/README.md`.

Dependency direction: `feature/* → core/*`; `core/*` only depends on `core:domain`; `composeApp`
depends on everything KMP; `:server` is independent.

### Conventions
- Version management is centralized in `gradle/libs.versions.toml`; reference deps via `libs.*`.
- Inter-module deps use typesafe project accessors (`projects.core.domain`, etc.).
- Convention plugins live in `build-logic/` (applied by `composeApp` via `libs.plugins.convention.cmp.application`).
- KMP library modules (`core:*`, `feature:*`) currently apply the raw
  `kotlin.multiplatform` + `android.kotlin.multiplatform.library` + `android.lint` plugins with a
  `jvm()` target added so the desktop app can depend on them. Extracting this into a
  `convention.kmp.library` plugin is a known TODO.
- i18n uses **Compose Resources**: `composeApp/src/commonMain/composeResources/values/strings.xml`
  is **Spanish (default)**; `values-en/strings.xml` is English; `values-id/strings.xml` is **Bahasa
  Indonesia**; `values-it/strings.xml` is **Italian**. Compose Resources selects by **device locale**
  automatically (not by the selected
  country — a Spanish-locale device browsing Indonesia keeps a Spanish UI; tying language to country is
  a deferred follow-up). Generated accessors live under `com.jyodroid.kunasismoayuda.resources.Res`.
- **Theme & branding.** `ui/theme/KunaTheme.kt` wraps `MaterialTheme` with harmonic **light AND dark**
  color schemes seeded from the **logo teal `#0F5E66`** (primary); teal-family secondary, a harmonic blue
  tertiary, teal-tinted neutrals, and a **strong red `error`** kept for SOS/danger in BOTH modes (never
  brand teal, never the pale Material dark-error tone). `KunaTheme(darkTheme = isSystemInDarkTheme())`
  switches `LightKunaColors`/`DarkKunaColors`; `App()` uses `KunaTheme { … }`. iOS `Info.plist` does NOT
  force `UIUserInterfaceStyle`, so iOS follows the system (satisfies iOS 27's dark-mode expectation).
  **Display name is "Kuna Ayuda"**: `app_name` in all three `strings.xml`, the desktop window title, the
  iOS usage strings, and — crucially for the iOS home-screen name — **`CFBundleDisplayName`/`CFBundleName`
  in `iosApp/Info.plist`** (the `PRODUCT_NAME` in `Config.xcconfig` stays `KunaSismoAyuda` — it names the
  `.app`/framework/bundle-id and must NOT change). **Brand assets** (single teal line-art mark: rescuers +
  family cradled in hands under an arch) live at `/Users/jyodroid/Documents/kuna-ayuda/images`; the full
  **iOS `AppIcon.appiconset`** is wired into `iosApp/iosApp/Assets.xcassets` (1024 no-alpha, App-Store-ready).
  **Android launcher icon**: adaptive icon = **teal Kuna mark on a WHITE background** (matches iOS),
  `mipmap-anydpi-v26/ic_launcher{,_round}.xml` → `@color/ic_launcher_background` (`#FFFFFF`) +
  `@drawable/ic_launcher_foreground` (a layer-list centring `@drawable/ic_launcher_logo` at 60×46dp in the
  108dp safe zone, **no `android:tint`**). ⚠️ **Pitfall (fixed):** the source logo PNG is teal-on-white
  (NOT truly transparent despite its `Logo-transparent` name), so `android:tint="#FFFFFF"` filled it into a
  solid white rectangle (broke launcher/splash/app-info). No monochrome layer (an opaque bitmap can't make
  a valid themed silhouette — needs a real transparent silhouette asset; deferred). minSdk 26 ⇒ legacy
  `mipmap-*/ic_launcher*.webp` never rendered (left, harmless). App **label is "Kuna Ayuda"**. The country
  picker is shown outside the app Scaffold, so it applies its own `safeDrawingPadding()` (else the title
  collides with the status bar / Dynamic Island).

### Data sources
- **USGS** FDSN GeoJSON (`earthquake.usgs.gov/fdsnws/event/1/query`), bounded to Colombia's bbox —
  reliable, no API key. Implemented in `server/.../upstream/UsgsSource.kt`.
- **SGC** (Servicio Geológico Colombiano, `datos.sgc.gov.co`) — authoritative national source; the
  `SgcSource` is a placeholder to be wired to the real GeoJSON/WFS endpoint (then it becomes primary).
- **Wildfires (second hazard, `GET /api/fires?country=`).** Same primary/fallback shape as quakes,
  aggregated by `services/FireService` (per-country bbox via `CountryBBoxes`, 60s cache) over two
  `upstream/FireSource`s: **NASA FIRMS** (`FirmsSource` — Area **CSV** API, VIIRS/MODIS active-fire
  points with FRP; needs a free **`FIRMS_MAP_KEY`** env — when unset the source is disabled and returns
  empty) and **GDACS wildfire events** (`GdacsFireSource` — the keyless fallback; uses the **EVENTS4APP**
  endpoint, since `SEARCH?eventlist=WF` returns 204, filtered to the country bbox). Normalized to
  `FireResponse` (frpMw/confidence/brightnessK/place), then **ranked by FRP and capped to
  `FireService.DEFAULT_MAX_FIRES` (200) most intense** (FIRMS can return thousands/country — Indonesia
  ~8k in 2 days — a heavy payload + map; the cap keeps the significant hotspots, recency breaks ties).
  Client: `core/domain Fire`/`FireRepository`,
  `core/data FireApi`, `ui/fires FiresViewModel` (featured fire + affected places, mirroring quakes),
  an Overview **fire bubble** → **`FiresListScreen`** (all active fires, `rankedFires()` most-relevant
  first) → `FireDetailScreen` for the tapped fire, and an orange **`MarkerKind.FIRE`** layer on the
  map. Verified live (DB-less): CO/ID/ES return GDACS fires, IT 0. Intensity is always named in text
  (`FireIntensity` LOW/MODERATE/HIGH), never colour alone. **Place labels:** FIRMS points are raw
  satellite hotspots with **no place name** (`FireResponse.place == null`), so when FIRMS is primary
  every fire would otherwise read "unknown location" — and FIRMS points land anywhere (the Amazon,
  offshore), often far from any city. The client names them honestly: `FiresViewModel.nearestPlace(fire)`
  → `FirePlace(name, distanceKm)` (nearest city in `CountryRegions`, no cap), and the shared
  `ui/fires firePlaceLabel(fire, near)` formats it — **"Cerca de <city>"** (≤40 km,
  `fire_place_near`), **"A N km de <city>"** (≤500 km, `fire_place_km`), else the **coordinates**
  (e.g. "3.9°S, 67.5°W") — never "unknown". Used by the Overview fire bubble + `FireDetailScreen`.
  For this to work nationwide, **`ColombiaRegions` was expanded from 16 to all 33 department capitals**
  (incl. Amazon/Orinoquía: Leticia, Inírida, Mitú, Puerto Carreño, San José del Guaviare, Florencia,
  Mocoa…) — also improves quake affected-region coverage. GDACS events already carry a place, so this
  only affects the FIRMS path.

### Landing site (`landing/` → served by `:server` at `/`)
A **plain Vite (vanilla TS)** marketing + legal site lives in **`landing/`** (repo root, an npm project,
**NOT** a Gradle module): `index.html` (home — features, 4 countries, a **Download** section with the
**Desktop/JVM app now** + Android/iOS "coming soon"), `privacy.html`, `terms.html`, shared
`src/{main.ts,i18n.ts→folded into main.ts,styles.css}`. **i18n is hand-rolled ES (default) + EN**: every
translatable chunk is written twice as `[data-lang="es"]`/`[data-lang="en"]` and `main.ts` shows the
active language (persisted in `localStorage`) — ES renders with no JS. Brand teal `#0F5E66`, placeholder
`public/logo.svg`+`favicon.svg` (swap for the real mark). The legal pages consolidate the app's inline
disclaimers and are the **canonical** privacy/terms (the app links to them from the **Guide** screen via
`core:data SITE_BASE_URL`/`PRIVACY_URL`/`TERMS_URL` — TODO: set the real domain; the app-store Privacy-URL
points here too). **Serving**: `routes/Routing.kt` adds `staticResources("/", "web") { default("index.html") }`
+ `/privacy`→`/privacy.html`, `/terms`→`/terms.html` redirects — non-`/api` paths are never touched by
`AppGate`/JWT and static never shadows `/api` (verified: `/`=200 even with the app-gate ON, `/api/**` still
gated). **Build/deploy**: `:server:buildLanding` (Exec `npm build` + Copy `dist`→`server/src/main/resources/web`)
is a **manual dev task** (needs Node) — it is **deliberately NOT wired into `processResources`** so Heroku's
Node-less JVM buildpack just packages whatever `web/` is present (recommended deploy option A). Re-run
`:server:buildLanding` after editing `landing/`; the fat jar bundles `web/` like any resource. The Desktop
app binaries are **not** bundled (too big for the slug) — the Download links point to configurable URLs
(GitHub Releases recommended). Placeholders to fill before launch: domain, contact email, responsible
party, governing-law jurisdiction, tagline, real logo, desktop download URLs — and a lawyer review.

## Accessibility

The app targets users with disabilities in a high-stress context. Conventions to keep:
- **Never convey meaning by color alone** — e.g. `MagnitudeBadge` announces magnitude + a severity
  word ("fuerte"/"strong") via `contentDescription`, and its text color is chosen for contrast.
- Mark section titles with `Modifier.semantics { heading() }` for screen-reader navigation.
- Loading/error containers use `liveRegion = LiveRegionMode.Polite` so state changes are announced.
- Interactive controls keep a **≥48dp** touch target (`heightIn(min = 48.dp)` on call buttons).
- **Calling is platform-specific** (`ui/platform` `PhoneCaller`, `expect/actual` — NOT the common
  `LocalUriHandler.openUri("tel:…")`, which silently failed on iOS when a user-typed number had
  spaces/dashes, since `NSURL(string:)` returned `nil`). `rememberPhoneCaller()` **sanitizes** the
  number first (keeps a leading `+`, digits, `*`/`#`; drops the rest) then: **Android** launches the
  system dialer via `ACTION_DIAL`; **iOS** opens `tel:` through `UIApplication.openURL:options:` when
  `canOpenURL` is true (a real iPhone) — on the **Simulator / iPad / iPod** (no Phone app, an Apple
  limitation `tel:` can't work around) it falls back to copying the number to the clipboard;
  **Desktop (JVM)** tries a `tel:` hand-off (macOS FaceTime / a registered VoIP app) and otherwise
  copies the number to the clipboard (no dialer on desktop). All the call buttons (help directory,
  shelters, board, search, SOS + responder) go through it. Map/`mailto:`/`https` links still use
  `LocalUriHandler` (they work cross-platform).
- All user-facing text is localized via Compose Resources (ES default / EN) and uses `sp`/theme
  typography so it scales with the system font size.

## Navigation (5 bottom tabs, kept ≤5 for accessibility). Tab icons are **monochrome bundled vector
drawables** (`composeResources/drawable/ic_*.xml`, rendered via `Icon(painterResource(...))` so the nav
bar tints them a single colour), not emoji.
**Resumen** (Overview — the **first tab / start destination**) · **Centros de ayuda** (map) · **Red de
ayuda** (mutual-aid board) · Refugios (shelters) · **Guía** (a `FilterChip` toggle between official
Channels and safety Tips — merged so we stay at 5 tabs). Nav labels are centered, single-line.
- The **map tab** (`DisasterMap`, `feature/map`) is **help points only** — it plots the official
  shelters (coloured by `ShelterType`, **labelled with the site name** via a `SymbolLayer`); the quake
  is **never drawn here**. The base map is **OpenFreeMap "Liberty"** vector tiles (`BaseStyle.Uri`,
  free/no-API-key OSM streets + place labels — set this or MapLibre falls back to the label-less
  `BaseStyle.Demo`, which is why the map looked blank before). By default it centres on the help points
  whose city is in the featured quake's affected regions (fallback: all points' centroid, then
  Colombia). A **"Cerca de mí"** FAB requests device location (`core:location` `LocationProvider`,
  on-demand so no unprompted permission), shows a **user dot** (`userLat/userLon`), filters to the
  **nearest 8** (`Geo.distanceKm`) and recentres — the camera follows any focus change through a
  `LaunchedEffect { cameraState.animateTo(...) }` (firstPosition only seeds frame 0). **Tapping a
  center opens a `ModalBottomSheet`** (`ShelterCard`) that now shows **distance from you** (when known)
  and a **"Cómo llegar"** button opening the coords in the device maps app via `LocalUriHandler`.
  The map needs network for tiles; bundling offline tiles (pmtiles) is a deliberate future task.
- The **Overview tab** (`ui/overview/OverviewScreen`, replaces the old list) summarizes the situation.
  It has a **country switcher** at the top and is wrapped in a **`PullToRefreshBox`** (pull to retry;
  the error state also shows a Retry button — a network hiccup or the server not-yet-reachable at
  launch is recoverable without a restart). It shows:
  a **quake bubble** (the **strongest** recent quake — `quakes.maxByOrNull { magnitude }`, NOT the
  impact-ranked first, so a sparsely-seeded country like Indonesia doesn't headline a nearby aftershock
  over a distant major quake; tapping it pushes `ROUTE_QUAKE_DETAIL` = `QuakeDetailScreen` with its
  **réplicas**, the *only* place
  quake detail appears), **shelters per location** (grouped by city, affected cities first), **aid
  network** offers vs requests (`BoardViewModel.summary`), and the **affected places** list. Réplicas
  come from `IdentifyAftershocksUseCase` (core/domain/usecase — spatial/temporal cluster heuristic,
  120 km / ±3 days, reusing `Geo.distanceKm`), surfaced via `QuakesViewModel.aftershocks(quake)`.
- **Guide tips** each show a monochrome illustrative vector (`composeResources/drawable/tip_*.xml`) in
  the `TipCard`. `SafetyTipsScreen` groups tips by `Phase` (Before/During/After plus a **Mental health**
  section — line 192 opción 4 / 106 / 123, normal reactions, calming techniques, supporting children,
  and a community IG account flagged as *not* an official line — and an **Animals** section —
  evacuating with pets, foundations receiving aid, vet care, lost-pet finder; community channels
  flagged as *not* official).
- The board has two pushed routes (not tabs): `board_create` (manual "new post" form — contact is
  opt-in with a public-contact disclaimer, #3) and `board_classify` (**"Pegar publicación"** — paste a
  social-media post's **text** → Claude structures it → the poster **reviews a preview and confirms**
  → queued for moderation; `ClassifyPostScreen` is two-step, "Analizar" → preview card + "Enviar a
  revisión"/"Editar", with a how-to for turning an IG/FB post into pasteable text and an "unreadable"
  message when a link/screenshot is pasted). **Device-gated resolve** (#4):
  posts this device created show a **"Marcar como resuelto"** button that closes them via
  `POST /api/board/{id}/resolve` — the owner secret from the create response is persisted locally in
  `core/data` `settings/PostOwnershipStore` (okio JSON, sibling to `CountryStore`), and
  `BoardViewModel` exposes the owned ids so only the owning device sees the affordance. **SOS is a persistent
  red button in the `TopAppBar`** (always one tap away on every screen — not a tab), routing to the
  `sos` screen with a large "PEDIR AYUDA" button + "Estoy a salvo" check-in.

## Roadmap
Done: **Overview home (quake bubble at the bottom + réplicas, shelters-per-location, aid counts,
affected places)** + **help-points-only MapLibre map (Android/iOS) with a real street base map
(OpenFreeMap "Liberty" vector tiles, no API key), site-name pin labels, "Cerca de mí" geolocation
(nearest-8 filter + user dot via `core:location`, camera follows focus via `animateTo`), and a
per-point "Cómo llegar"/distance callout** + **nationwide
official help points (Cruz Roja seccionales per department capital, plus V8 Chocó/Valle affected-place
points)** + offline **guide (channels + tips: Before/During/After + Mental health + Animals, with
per-tip illustrations)** + **moderated shelters/acopios** + **mutual-aid resource board** +
**geolocated SOS + "I'm safe" check-in** + **offline SOS outbox (persist + auto-retry)** + **AI
paste-and-classify board flow (Claude → moderated pending posts)** + **moderator login + in-app
moderation queue** + **monochrome bundled-vector tab icons** + **accessibility pass**. Server
Postgres/Exposed/Flyway is live (8 migrations). **iOS location (CoreLocation) is done** — SOS now
attaches precise coordinates on iOS too. **The full iOS app now compiles and the framework links
via Gradle** (`:composeApp:linkDebugFrameworkIosSimulatorArm64`) after fixing the `feature:map`
source-set wiring; the one-time Xcode SPM step for MapLibre's native SDK **is done** (package
`maplibre-gl-native-distribution` 6.25.1, product `MapLibre`; `Config.xcconfig` sets the required
`OTHER_LDFLAGS = -framework ComposeApp -framework MapLibre` order). **The in-app SOS responder view is
done** — a logged-in moderator opens it from the moderation queue and sees submitted SOS alerts + "I'm
safe" check-ins (filterable), with map/call actions, an **attended/notified lifecycle** (archive /
reopen / permanent-delete, `handled_at`/`handled_by`), and a **pending-vs-handled stats row**. Next:
desktop map (Java 25); push notifications; a map view of active SOS; Room offline cache. (**Done:**
in-app moderator form to add shelters/collection points with a map-tap location picker; **fixed** the
shaded fat jar applying 0 Flyway migrations — see the Flyway + fat jar note — so Heroku deploys now
migrate correctly; **whistle/flashlight SOS** — the offline emergency beacon, see `:core:beacon` below.)

**Offline emergency beacon** (`:core:beacon`, on the SOS screen): a battery-guarded **light + sound**
beacon that flashes the torch and sounds an alarm in the international **SOS Morse** pattern
(`··· ——— ···`) so nearby rescuers can find someone when there's no signal — **fully offline** (local
torch/speaker only, no network). Battery-first by design (the user's explicit constraint): SOS Morse is
**duty-cycled** (emitter off ~60% of each cycle, far less drain than a steady strobe/siren) and it
**auto-stops after a bounded ~30s burst** (re-tap to repeat) with a **battery-warning confirmation**
before it starts. Shared logic (SOS-morse loop, ~30s cap, light/sound live toggles, guaranteed
hardware-off in `finally` on cancel/completion) lives in commonMain `EmergencyBeacon` over an
`expect createBeaconDevice()`; **Android** = `CameraManager.setTorchMode` torch + `ToneGenerator`
(STREAM_ALARM), needs `AndroidBeaconContext` set in MainActivity; **iOS** = `AVCaptureDevice` torch +
looping `AVAudioPlayer` (Playback session, sounds through the silent switch); **Desktop (JVM)** = no
torch + `javax.sound` Clip. The tone is a pure-Kotlin generated sine WAV (`ToneWav`) consumed by
iOS/Desktop. Wired via DI `single { createBeaconDevice() }` + `single { EmergencyBeacon(get()) }` into
`SosViewModel` (start/stop as a `viewModelScope` job — cancel = stop). Covered by `MorseTest`,
`ToneWavTest`, `EmergencyBeaconTest` (fake device + virtual time asserts auto-stop + cleanup).

### Architecture: layer-first, deliberately (decided 2026-08-16)
The app is **layer-first**, not feature-first: the backbone is the `core:*` layer modules
(`domain`, `data`, `presentation`, `designsystem`, `location`, `media`), and **feature UI lives in
`composeApp`** as `ui/*` packages (`ui/overview`, `ui/quakes`, `ui/board`, `ui/shelters`, `ui/help`,
`ui/tips`, `ui/guide`, `ui/sos`, `ui/auth`, `ui/moderation`, `ui/admin`, `ui/search`) with their VMs +
`di` wiring. **A module is created only when platform code or reuse demands it** — hence `feature:map`
and `core:media` (expect/actual) and `core:location`, but everything else stays a package. Consistency
pass on 2026-08-16: the old half-module `:feature:quakes:domain` (domain-only, UI in composeApp) was
**folded into `core:domain/usecase`**, and the `media` image-picker (expect/actual platform code that
had been sitting inside composeApp) was **promoted to `:core:media`**. Full per-feature `:feature:*`
extraction remains a deliberate non-goal for a solo/pre-launch project (the payoff — build parallelism,
compile-enforced boundaries — mostly matters at larger scale); revisit post-launch, and the
`convention.kmp.library` plugin (pending) is the enabler if we ever do.

**Lost & Found / reunification** (`ui/search`): reachable from the aid board ("Búsqueda y reencuentro"),
it lists community reports of **lost/found pets and people** (`SearchViewModel` over `SearchRepository`
→ `/api/search`), filterable by subject (pet/person) and state (lost/found), **direct-post** (no
moderation). Reports carry an **optional photo**: `core:media`'s `ImagePicker` (expect/actual — Android system
photo-picker + `TakePicturePreview` camera; iOS `UIImagePickerController`; Desktop = file chooser, no
camera; all downscale to ~1280 px JPEG) captures it, `SearchApi.uploadPhoto` multipart-uploads to
`POST /api/photos`, and thumbnails load via **Coil 3** (`AsyncImage`) through the app's shared Ktor
client (so `X-App-Key` is attached — set via `setSingletonImageLoaderFactory` in `App()`). The aid board
also gained a **resource-type filter** (kept in the hoisted `BoardViewModel`, so it survives tab switches).
