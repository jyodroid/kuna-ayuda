# Google Play — Data safety form answers (Kuna Ayuda)

A field-by-field draft based on what the app actually does. Review before submitting; update if the app
changes. Key facts that shape every answer:

- **Regular users are anonymous** — no account, no login. Only **moderators** authenticate (email +
  password). No analytics, ads, tracking SDKs, advertising ID, or device IDs.
- Almost all data is **user-initiated** (you tap SOS, or write a post) — nothing is collected in the
  background.
- Data goes to **our own server** (Heroku) over HTTPS. "Collected" = sent to us. "Shared" (Play's
  definition) = sent to a **third-party company**. The only third-party sharing is the **paste-and-
  classify** text, which is sent to **Anthropic (Claude)** and **Google (Fact Check Tools)** to
  structure it — declared under Messages/UGC below.
- Public visibility: contact info and photos you *choose* to attach to a public post are visible to
  other users while the post is live; they're scrubbed when it's closed and posts auto-expire in 30 days.

## Overview questions

- **Does your app collect or share any of the required user data types?** → **Yes**
- **Is all of the user data collected by your app encrypted in transit?** → **Yes** (HTTPS/TLS)
- **Do you provide a way for users to request that their data is deleted?** → **Yes**, and also select
  **automatic deletion** if offered. Justification (both apply, per Google's "request deletion OR
  automatic deletion within 90 days" rule):
  - Request: email kunaayuda@gmail.com; the creating device can resolve/close its own post in-app.
  - Automatic: aid-network/search posts auto-expire after **30 days** (well under 90) and contact info +
    the owner secret are scrubbed on close.

## Data types — what to declare

Legend: Collected = sent to our server · Shared = sent to a third-party company · Optional = user chooses
to provide it (not required to use the app) · Purpose = choose "App functionality" for all of these.

### Location
- **Approximate location** — Collected: **Yes** · Shared: **No** · Optional: **Yes** · Purpose:
  App functionality. (Attached to an SOS or "I'm safe" check-in, only if the user allows location.)
- **Precise location** — Collected: **Yes** · Shared: **No** · Optional: **Yes** · Purpose:
  App functionality. (Same SOS flow; device GPS.)

### Personal info
- **Name** — Collected: **Yes** · Shared: **No** · Optional: **Yes** · Purpose: App functionality.
  (Optional contact name on an aid-network / Lost & Found post; public while the post is live.)
- **Email address** — Collected: **Yes** · Shared: **No** · Optional: **Yes** · Purpose:
  App functionality + Account management. (Optional contact email on a post; and moderator sign-in.)
- **Phone number** — Collected: **Yes** · Shared: **No** · Optional: **Yes** · Purpose:
  App functionality. (Optional contact phone on SOS / board / search posts; public while live.)

### Photos and videos
- **Photos** — Collected: **Yes** · Shared: **No** · Optional: **Yes** · Purpose: App functionality.
  (Optional photo on a Lost & Found report; public while the report is live.)

### Messages
- **Other user-generated content** — Collected: **Yes** · Shared: **YES** · Optional: **Yes** ·
  Purpose: App functionality. (Aid-network/search post text and SOS messages the user writes. The
  **paste-and-classify** text is **shared with Anthropic and Google** to structure it — this is the one
  "Shared" item; declare the third-party processing here.)

### Data types to mark as NOT collected
- Financial info — No
- Health and fitness — No
- Calendar / Contacts — No
- App activity (searches, other actions logged for analytics) — No
- Web browsing history — No
- App info and performance (crash logs, diagnostics) — No (no analytics/crash SDK)
- **Device or other IDs** — No (no advertising ID, no device ID; the app key is a static app-wide value,
  not a per-user/per-device identifier)

## Data usage & handling notes (for the free-text / per-type detail)
- Purpose for every collected type: **App functionality** (email also **Account management** for the
  moderator login).
- **None** of the data is used for **advertising or marketing**, **analytics**, or **personalization**.
- Data is **not sold**.
- Collection is **optional/user-initiated** for every type above.

## If the form asks about account creation
- Regular users: **no account required**. Only moderators create an account (email + password), which is
  a small, separate group — declare email under Personal info as above.
