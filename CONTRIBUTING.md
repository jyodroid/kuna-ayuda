# Contributing to Kuna Ayuda

Thanks for wanting to help. Kuna Ayuda is a **disaster-relief** tool — people may rely on it in a crisis,
so accuracy, safety, accessibility, and privacy come before features or cleverness. Please keep that in
mind in everything you contribute.

By contributing, you agree that your contributions are licensed under the project's [MIT License](LICENSE).

## Ways to help

You don't have to write Kotlin to contribute:

- **Translations** — improve or complete the four locales (ES / EN / ID / IT) in
  `composeApp/src/commonMain/composeResources/values*/strings.xml`, or the landing site (`landing/`).
- **New-country data** — region/city lists (`core/domain/.../model/*Regions.kt`), verified official
  **emergency numbers** (`core/domain/.../model/EmergencyDirectory.kt`), and official **help points**
  (a Flyway seed migration). See "Adding a country" below.
- **Tests** — pure logic in `core:domain`, the `core:data` mappers, and server `services/` is the
  highest-value place to add coverage.
- **Accessibility, docs, design, bug reports.**

## Ground rules

- **This is not an emergency service** and must never imply otherwise. Don't weaken the safety
  disclaimers or the "call official numbers" guidance.
- **Help points are official/verified only.** Never seed unverified locations as official; the
  mutual-aid board is the place for community (clearly-unverified) content.
- **Protect personal data.** Don't add tracking/analytics SDKs. Keep contact info opt-in and public only
  while a post is live (it's scrubbed on close). Regular users stay anonymous — no accounts.
- **Verify emergency numbers** against an authoritative public source and cite it in the PR.

## Development

Setup and build/run commands are in the [README](README.md#getting-started). In short: JDK 17, Android
Studio, `./gradlew :server:run` (backend + landing), `./gradlew :composeApp:run` (desktop).

**Before opening a PR:**

1. Build what you touched — e.g. `./gradlew :composeApp:compileKotlinJvm`,
   `:composeApp:compileDebugKotlinAndroid`, `:composeApp:compileKotlinIosSimulatorArm64`, `:server:build`.
2. Run the tests: `./gradlew :core:domain:jvmTest :core:data:jvmTest :server:test`.
3. **Add tests** for new logic (a bug fix should come with a test that fails without it).

## Conventions

- **Match the surrounding code** — naming, comment density, and idioms. Read `CLAUDE.md` first; it
  documents the architecture and the non-obvious decisions (layer-first, per-country ownership, the
  Flyway fat-jar fix, spend caps, etc.).
- **Architecture**: `core:domain` stays pure (no framework deps); feature UI lives in `composeApp/ui/*`;
  create a module only when platform code (`expect/actual`) or real reuse demands it.
- **i18n**: every user-facing string goes in **all four** `strings.xml` locales (Compose Resources needs
  an explicit per-string import) — no hard-coded UI text.
- **Accessibility**: never convey meaning by color alone (pair it with text), keep ≥48dp touch targets,
  mark headings, and use `sp`/theme typography so text scales.
- **Migrations**: never edit an applied `V*.sql` (Flyway checksum) — add a new `V{n}__*.sql`.
- **Commits/PRs**: small, focused, with a clear description of *what* and *why*. Reference any issue.

## Adding a country

1. Add it to the `Country` enum (`core/domain`) — code, localized names, flag, map centroid/zoom.
2. Add its region/city list (`*Regions.kt`) and wire `CountryRegions.of`.
3. Add its verified emergency numbers to `EmergencyDirectory.kt`.
4. Add its quake bounding box to the server (`upstream/CountryBBoxes`).
5. Seed real official help points via a new Flyway migration.
6. Ensure all new strings exist in the four locales; build all targets.

## Security

Found a vulnerability? **Please don't open a public issue.** Email the maintainer (see the repository
profile / landing-page contact) with details, and give us a reasonable window to fix it before disclosure.

## Code of conduct

Be kind, patient, and constructive. Assume good intent. This is a volunteer, public-good project — treat
contributors and users with respect.
