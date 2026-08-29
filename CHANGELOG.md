# Changelog

All notable changes to Kuna Ayuda. Format based on [Keep a Changelog](https://keepachangelog.com/);
this project uses [Semantic Versioning](https://semver.org/). The version lives in
`gradle/libs.versions.toml` (`desktopPackageVersion`) and is enforced against the git tag by CI.

## [1.5.1] — 2026-08-29

### Added
- **Editable classify preview.** After pasting a post or a screenshot, you can now correct what the AI
  extracted — kind, type, region, description, contact name and phone — before sending it for review.
  (The moderator signals and drop-off points stay read-only.)

### Fixed
- **Phone with two numbers.** When a post listed two phone numbers, the "call" button dialed a broken,
  merged number. The AI now keeps a single callable number in the contact field (all numbers stay in
  the description), and you can edit it in the preview.

## [1.5.0] — 2026-08-26

### Added
- **"Actividad reciente" quake bubble on Overview.** A fresh earthquake that's weaker than a bigger
  recent one used to be invisible (the home headline always shows the *strongest* quake). Now a second
  bubble surfaces the **newest** quake when it happened in the last 48 h and isn't already the headline,
  with **relative time** ("hace 3 h") and a **"Hoy"** badge. The headline (strongest) bubble also shows
  the "Hoy" badge when it's itself less than a day old. Tapping either opens that quake's detail and
  aftershocks.

## [1.4.0] — 2026-08-25

### Added
- **Public "I'm safe" list.** An "Estoy a salvo" check-in is now a public reassurance post so your
  family can see you're OK. It requires a **name** and shows a **Publicar / Cancelar** confirmation
  before posting — accept publishes, cancel discards (nothing is stored). It appears under **Búsqueda y
  reencuentro → A salvo** as **name · city · "hace N min"**, scoped to the selected country. Only
  name + city + time are ever public — never precise location, phone, or message.

### Changed
- The moderator **SOS responder view is now SOS-only** (people who need help); safe check-ins are the
  public list, and are still moderatable from the web console.
- Retention unchanged: safe check-ins hard-purge at 60 days; a moderator can delete one.

### Server
- Migration **V22** adds `display_name` to `sos_reports` and a public `GET /api/sos/safe` endpoint.
  **Backwards compatible:** older app versions keep working — the new request fields are optional, and
  a check-in without a name simply doesn't appear on the public list. Runs automatically on deploy.

## [1.3.0] — 2026-08-22

### Added
- **Preview before publishing.** The aid-board create form and the Lost & Found (Búsqueda y reencuentro)
  create form now have a local **Vista previa → Publicar / Editar** review step before anything is sent,
  so posters can catch mistakes before their post goes live.
- **Image-first paste-and-classify.** Because Instagram blocks copying a post's text, the "Pegar
  publicación" flow now also accepts a **screenshot/photo** (gallery or camera): Claude reads it with
  vision, extracts the same structured post, and the poster reviews a preview and confirms — served from
  cache on confirm, no extra paid call.
- **Moderator risk flags.** Classification now surfaces moderator-facing caution flags
  (asks-for-money / unverified-claim / no-source) on the classify preview, the app moderation queue, and
  the console — a **signal only, never an auto-reject** (like the fact-check note).
- **Moderator instant-delete of published content.** Moderators can now remove a **live** aid-board post
  (new **Publicados** tab in the app + console) and a **Lost & Found** report (in-app **Eliminar** on
  each card, matching the console) on the spot — not only pending items.

### Changed
- **Data retention.** User content now has a full lifecycle: ACTIVE (0–30 days) → CLOSED + contact
  scrubbed (30–60 days) → **permanently deleted (>60 days)**. Aid-board posts, Lost & Found reports (and
  their photos), and SOS reports are hard-purged after 60 days; official help points are kept. The
  landing privacy policy states the 60-day deletion.
- CI now rebuilds the landing site **and** the moderator console into the server on every deploy, so the
  deployed jar always serves the current versions.

### Server
- Migration **V21** adds `risk_flags` to `resource_posts` and `classify_cache`; the classify prompt
  version bumped to `v5` (image intake + risk flags). Runs automatically on deploy.

## [1.2.0] — 2026-08-21

### Added
- **Accessible safety tips.** Tapping a Guide tip now opens a detail sheet with:
  - a **"Escuchar" / Listen** button that reads the tip aloud via device text-to-speech (Android
    `TextToSpeech`, iOS `AVSpeechSynthesizer`; hidden on desktop), for low-vision and non-reading users;
  - **wordless step-by-step illustration storyboards** for six high-value tips — *Before*, *During*,
    *After* an earthquake, plus *Pets*, *Calming techniques*, and *Supporting children* — with a short
    localized caption per step (ES/EN/ID/IT) that doubles as the image's accessible description.
- **Drop-off / collection points** in classified aid-board posts: the paste-and-classify flow now
  extracts a structured list of where to bring or pick up resources (name / address / hours) and shows
  it as a clear "Puntos de recepción" block on the board card, classify preview, and moderation queue.
- **SOS proximity grouping** in the moderator responder view: with the moderator's location, submitted
  SOS reports group under **Cerca (≤2 km) / Misma zona (≤25 km) / Lejos — delegar (>25 km) / Sin
  ubicación**, each with a per-card distance, so nearby alerts stand out and far ones can be delegated
  (empty test taps land in "Sin ubicación").

### Changed
- Landing site: added `canonical` + Open Graph `og:url`/`og:image` (a 1200×630 card) + Twitter card so
  shared links show a proper preview; set the canonical domain to `kunaayuda.org` across home/privacy/terms.

### Server
- Migration **V20** adds `collection_points` to `resource_posts` and `classify_cache`; the classify
  prompt version bumped to `v4` (a previously-pasted post re-extracts, now with drop-off points). Runs
  automatically on deploy.

## [1.1.1] — 2026-08-20

### Changed
- Overview: the "Affected places" list now shows a subtitle — "Distance from the earthquake epicenter" —
  so it's clear the list relates to the earthquake (not wildfires) and what the distance measures.

## [1.1.0] — 2026-08-20

### Added
- **Peru (PE)** as the 5th supported country — live earthquake + wildfire feeds, help-points map, aid
  board, SOS and guide. Peruvian emergency numbers (105 Policía / 116 Bomberos / 106 SAMU / 115 INDECI
  + Línea 113 for mental health) and official seeded help points (INDECI, Cruz Roja Peruana, CGBVP).
- Moderator sessions now **sign out automatically when the token expires** (a 401 on an authenticated
  request), returning to the login screen with a "session expired" message instead of a generic error.

### Fixed
- Landing page: the "Kuna Ayuda" wordmark no longer overlaps the nav on small mobile screens.

## [1.0.1] — 2026-08-19

### Fixed
- Dark mode: the "+" and check icons (aid board / search FABs, verified-shelter chip) were invisible —
  replaced emoji glyphs with theme-tinted vector icons.
- SOS emergency beacon: the countdown now shows the seconds remaining (was a literal `%d`).
- Released apps now default to the **production API** instead of `localhost`; local dev builds still use
  a local server automatically (debug/run only), so a release can never ship pointing at a dev machine.
- Wildfire list de-duplicates adjacent NASA FIRMS hotspots — one wildfire (many pixels) shows as one
  entry instead of many identical-looking rows.
- Server: fixed the production boot crash (CORS rejected an `ALLOWED_ORIGIN` that included the scheme)
  and Heroku Postgres SSL (`sslmode=require`).

### Added
- App version shown in the Overview footer, generated from a single source (`libs.versions.toml`).
- CI guard that fails a release if the git tag doesn't match `desktopPackageVersion`.
- Android release signing config (via a gitignored `keystore.properties`) for Play uploads.

### Changed
- Branding: app name "Kuna Ayuda", branded launcher/app icon, and the real logo as the web favicon.
- Custom domain: the app, landing, and legal pages now use `https://kunaayuda.org`.

## [1.0.0] — 2026-08-19

### Added
- Initial public release. Live earthquake feed (region-prioritized, with aftershocks), active wildfire
  feed, official help-points map with "near me", neighbor-to-neighbor mutual-aid board (with AI
  paste-and-classify), Lost & Found for people and pets, geolocated SOS with an offline retry queue,
  an offline light + sound emergency beacon, and an offline safety guide.
- Multi-country: Colombia, Indonesia, Spain, Italy. Localized ES / EN / Bahasa Indonesia / Italian.
- Targets: Android, iOS, Desktop (JVM) + a self-contained Ktor backend. Desktop installers
  (.dmg / .msi / .deb) and the server jar are published via GitHub Releases.
