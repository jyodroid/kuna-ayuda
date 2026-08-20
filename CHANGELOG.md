# Changelog

All notable changes to Kuna Ayuda. Format based on [Keep a Changelog](https://keepachangelog.com/);
this project uses [Semantic Versioning](https://semver.org/). The version lives in
`gradle/libs.versions.toml` (`desktopPackageVersion`) and is enforced against the git tag by CI.

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
