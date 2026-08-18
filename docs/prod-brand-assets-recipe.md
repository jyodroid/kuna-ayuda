# Kuna Sismo Ayuda — Brand & Store Asset Recipe

A step-by-step, copy-paste recipe for a solo Android engineer (not a designer) to create every
visual/brand asset needed to ship **Kuna Sismo Ayuda** to the **Google Play Store** and the
**Apple App Store**. Art is generated with **Google Gemini** and processed in **Android Studio** and
**Xcode**.

> **Scope:** brand mark → launcher / notification / splash / store / landing assets. In-app iconography
> (`ic_*.xml` tab icons, `tip_*.xml` illustrations) is already done and is **out of scope** — do not
> generate new in-app art.

**Brand personality to keep consistent everywhere:** humanitarian, calm, trustworthy, accessible,
reassuring. High-stress context → **high contrast, never rely on color alone, simple and bold**. Works
for Colombia, Indonesia and Spain (no country-specific symbolism in the mark).

---

## 0. How to work with Gemini (read this first — caveats)

Gemini produces **raster** images, not clean vectors. Expect these limitations and plan around them:

- [ ] Assume Gemini output is **not a clean vector**, **not perfectly transparent**, and **not perfectly
      centered**. You will always clean it up afterward.
- [ ] **Generate large** (ask for 2048×2048 or "high resolution"), then downscale — never trust Gemini's
      exact pixel dimensions or aspect ratio. Do final resizing in Android Studio / Xcode / a simple tool
      (Preview, GIMP, ImageMagick, [redketchup.io/image-resizer](https://redketchup.io) or similar).
- [ ] Prefer a **simple, bold, high-contrast mark** over a detailed illustration. Simple marks: stay
      legible at 24×24 px, survive monochrome derivation (notification + themed icons), and are easier to
      clean up.
- [ ] Gemini won't reliably give a truly transparent PNG. Generate on a **flat, uniform background you
      don't want** (pure white or pure magenta `#FF00FF`) and remove it later with a background remover
      ([remove.bg](https://www.remove.bg), Photoshop/GIMP "select by color", or `magick ... -fuzz 10%
      -transparent white`).
- [ ] Gemini frequently **adds text/letterforms even when told not to**. Regenerate until clean, or crop
      it out. Never ship generated text — all real text is added by the stores/OS.
- [ ] Keep a **master source** (see §12): the cleaned, centered 2048×2048 (or larger) PNG per concept,
      plus — ideally — a hand-traced **SVG** of the final chosen mark for crisp scaling.

---

## 1. Pick ONE brand-mark concept (do this before generating anything)

Generate a few options from the three concepts below, choose **one**, and stay consistent across every
asset. Do **not** mix concepts between launcher / splash / store.

**Palette (suggested — adjust once, then reuse the exact hex everywhere):**

| Token | Hex | Use |
|---|---|---|
| Brand primary (deep teal/blue) | `#0F5E66` | Icon background, splash background |
| Brand accent (warm amber) | `#F4A825` | Optional highlight / "help" spark |
| Ink (near-black) | `#10201F` | Dark foreground on light |
| Paper (near-white) | `#F7FAFA` | Light foreground / monochrome glyph |

> Calm teal/blue reads as trust + safety; a single warm accent signals help/rescue without alarm-red
> (reserve red for the in-app SOS button, not the brand). Verify contrast in §11.

### Concept A — "Sheltered wave" (recommended)
A simple shelter/roof (or cupped hands) arc **over** a single seismic wave line. Evokes protection +
quake in one bold glyph. Best monochrome derivation.

### Concept B — "Pulse pin"
A map/location pin whose interior is a seismic pulse/heartbeat line. Ties to the app's map + help-points
identity. Slightly busier at tiny sizes.

### Concept C — "Concentric care"
Concentric arcs (seismic ripples) radiating from a solid center dot, with the top arc thickened into a
protective canopy. Very abstract, scales down beautifully, extremely clean monochrome.

- [ ] Generate 3–5 candidates for each concept (prompts below).
- [ ] Pick **one** concept + one candidate as the master mark.
- [ ] Lock the palette hex values above (edit them if you prefer different brand colors).

### Brand-mark generation prompts (run all three, pick one)

**Concept A — Sheltered wave**
```
A minimalist, flat vector-style app icon logo mark: a simple rounded protective shelter arc (like a
roof or cupped hands) curving over a single smooth seismic/sound wave line beneath it. Bold, geometric,
high-contrast, thick even strokes, symmetrical, centered. Deep teal-blue (#0F5E66) glyph with one small
warm amber (#F4A825) accent on the wave crest. Perfectly flat 2D, no gradients, no shadows, no 3D, no
bevels. Solid pure white background. Generous empty margin around the mark (mark fills ~65% of the
frame, centered). Humanitarian, calm, trustworthy feel. Square 1:1 composition, high resolution
(2048x2048).
Do NOT include any text, letters, numbers, or words. No photorealism, no people's faces, no flags, no
country symbols, no clutter, no fine detail, no drop shadows, no gradient meshes, no border frame.
```

**Concept B — Pulse pin**
```
A minimalist, flat vector-style app icon logo mark: a single rounded map location pin, and inside its
circular body a simple horizontal seismic pulse / heartbeat line. Bold, geometric, high-contrast, thick
even strokes, symmetrical, centered. Deep teal-blue (#0F5E66) with one warm amber (#F4A825) accent
segment on the pulse peak. Perfectly flat 2D, no gradients, no shadows, no 3D. Solid pure white
background. Generous empty margin (mark fills ~65% of the frame, centered). Calm, trustworthy,
humanitarian feel. Square 1:1 composition, high resolution (2048x2048).
Do NOT include any text, letters, numbers, or words. No photorealism, no faces, no flags, no country
symbols, no clutter, no fine detail, no drop shadows, no border frame.
```

**Concept C — Concentric care**
```
A minimalist, flat vector-style app icon logo mark: three concentric seismic ripple arcs radiating
upward and outward from a single solid center dot, with the topmost arc thickened into a gentle
protective canopy. Bold, geometric, high-contrast, thick even strokes, symmetrical, centered. Deep
teal-blue (#0F5E66) with the center dot in warm amber (#F4A825). Perfectly flat 2D, no gradients, no
shadows, no 3D. Solid pure white background. Generous empty margin (mark fills ~60% of the frame,
centered). Calm, reassuring, humanitarian feel. Square 1:1 composition, high resolution (2048x2048).
Do NOT include any text, letters, numbers, or words. No photorealism, no faces, no flags, no country
symbols, no clutter, no fine detail, no drop shadows, no border frame.
```

- [ ] Save the chosen raw output as `assets/source/mark-master-raw.png`.
- [ ] Clean it: remove background → **transparent PNG**, center on a square canvas, trim stray text.
      Save as `assets/source/mark-master.png` (transparent, ≥2048×2048).
- [ ] (Recommended) Hand-trace the final mark to **SVG** (`assets/source/mark-master.svg`) for crisp
      scaling — e.g. Inkscape "Trace Bitmap", or redraw with the simple geometry.

---

## 2. Android launcher icon (adaptive + monochrome)

Android launcher icons are **adaptive**: two layers, each **108×108 dp**, with only the center
**72×72 dp** guaranteed visible (the outer ring is cropped to circle/squircle/rounded-square by each
launcher). Android 13+ adds an optional **monochrome** layer for themed icons.

**Safe zone (critical):** keep all essential glyph content inside the central **66 dp diameter** circle
(≈ inner 61%). Anything outside the inner 72×72 dp **will be clipped**.

### 2a. Generate the foreground layer (transparent glyph)
Use your cleaned `mark-master.png` OR regenerate a foreground-only version:
```
A single minimalist flat vector app-icon glyph: [describe your chosen mark, e.g. "a protective shelter
arc over a seismic wave line"], deep teal-blue (#0F5E66) with a small warm amber (#F4A825) accent. Bold
thick strokes, geometric, perfectly centered, flat 2D, no gradient, no shadow. FULLY TRANSPARENT
background (PNG alpha). The glyph occupies only the central 60% of the square canvas with large empty
transparent margins on all sides. Square 1:1, high resolution (2048x2048).
Do NOT include text, letters, numbers, background color, shadows, or frame.
```
- [ ] Export foreground as a **transparent PNG**, glyph within the central ~60%.
      Save `assets/source/ic_launcher_foreground.png`.

### 2b. Background layer
Keep it simple — a **solid brand color** background reads best and is most accessible.
- [ ] Create a solid `#0F5E66` background (a flat color; no art needed). You can let Image Asset Studio
      use a color, or supply a 1024×1024 solid PNG.

### 2c. Monochrome layer (Android 13+ themed icons)
The themed-icon layer must be a **single-color silhouette** of your glyph on transparency; the system
recolors it. Derive it from the foreground:
- [ ] Make a flat **solid-color silhouette** of the mark (fill everything one opaque color, e.g. black),
      transparent background, same 72 dp safe zone. Save `assets/source/ic_launcher_monochrome.png`.
      (Tip: in GIMP, lock alpha and bucket-fill the glyph black; or generate with the prompt below.)
```
A single flat solid-black silhouette of [your chosen mark], one uniform color, no internal detail
lines, no gradient, no outline. FULLY TRANSPARENT background. Centered within the middle 60% of a square
canvas. Square 1:1, high resolution (2048x2048). No text, no shadow, no frame.
```

### 2d. Import into Android Studio — Image Asset Studio
- [ ] In Android Studio: **right-click `res` → New → Image Asset**.
- [ ] Icon Type: **Launcher Icons (Adaptive and Legacy)**.
- [ ] **Foreground Layer** → Asset Type: Image → select `ic_launcher_foreground.png`. Adjust **Resize**
      so the glyph sits inside the safe-zone circle preview (no clipping).
- [ ] **Background Layer** → Color `#0F5E66` (or your background image).
- [ ] **Legacy** tab: leave generation on (produces round + square legacy `mipmap` icons for old
      Android).
- [ ] Finish. It auto-generates `mipmap-mdpi … mipmap-xxxhdpi` (48/72/96/144/192 px) + the
      `ic_launcher.xml` / `ic_launcher_round.xml` adaptive descriptors + WebP density variants.
- [ ] **Monochrome:** Image Asset Studio may not expose a monochrome slot in your version. If not, add
      it manually: place `ic_launcher_monochrome.png` (or a vector) and reference it in
      `res/mipmap-anydpi-v26/ic_launcher.xml`:
      ```xml
      <adaptive-icon ...>
          <background android:drawable="@color/ic_launcher_background"/>
          <foreground android:drawable="@drawable/ic_launcher_foreground"/>
          <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
      </adaptive-icon>
      ```
- [ ] Run on an **Android 13+** device/emulator, enable **Themed icons** (Settings → Wallpaper & style)
      and confirm the monochrome layer reads clearly.

### 2e. Play Store 512×512 hi-res icon
- [ ] Export a **512×512 px, 32-bit PNG** of the full-color icon **with** the background layer baked in
      (a flat square — Play applies its own rounded mask). No transparency needed here; a solid
      background is fine. Save `assets/store/play/icon-512.png`.
      (Quick way: render the adaptive icon at 512, or resize `mark-master` centered on a `#0F5E66`
      512×512 square.)

---

## 3. Android notification icon (white silhouette — push is planned)

The status-bar/notification small icon **must be a white, transparent, monochrome silhouette**. Android
takes only the **alpha channel** and tints it. A colored logo → the infamous **solid gray/white blob**.

- [ ] Create a **white** silhouette of the mark on a **fully transparent** background, simple and bold
      (thin detail vanishes at 24 dp). Keep ~2 dp padding.
- [ ] Provide as a **vector drawable** (best) at `res/drawable/ic_stat_notify.xml`, OR density PNGs:
      mdpi 24×24, hdpi 36×36, xhdpi 48×48, xxhdpi 72×72, xxxhdpi 96×96 px (all white-on-transparent).
- [ ] You can generate it in Image Asset Studio: **New → Image Asset → Icon Type: Notification Icons**
      (it forces the white-silhouette treatment for you). Supply the black/white silhouette PNG.
- [ ] Wire it into push (FCM): set it as the default in the manifest so notifications use it:
      ```xml
      <meta-data android:name="com.google.firebase.messaging.default_notification_icon"
                 android:resource="@drawable/ic_stat_notify"/>
      ```
      and/or `NotificationCompat.Builder(...).setSmallIcon(R.drawable.ic_stat_notify)`.
- [ ] Set a notification accent color (`setColor(...)` / manifest `default_notification_color`) —
      e.g. `#0F5E66` — since the tint applies to the silhouette on some surfaces.
- [ ] Test: post a real notification on Android 5+ and confirm it is a crisp glyph, **not a gray blob**.

Generation prompt (if not deriving from the mark):
```
A flat solid-white silhouette of [your chosen mark], one uniform white color, no internal detail, no
outline, no gradient. FULLY TRANSPARENT background. Simple and bold so it stays legible at very small
size (24px). Centered with small margin. Square 1:1, high resolution (1024x1024). No text, no shadow,
no color, no frame.
```

---

## 4. iOS app icon (single 1024×1024 master)

- [ ] Produce **one** master icon: **1024×1024 px, PNG or JPEG, sRGB, NO alpha channel / NO
      transparency** (App Store **rejects** icons with an alpha channel), **NO rounded corners** (the
      system masks the corners — supply a full square), no drop shadow.
- [ ] Bake the **solid brand background** (`#0F5E66`) behind the glyph so it's a flat opaque square.
      Save `assets/store/appstore/icon-1024.png`.
- [ ] Strip alpha if present: `magick icon-1024.png -background '#0F5E66' -alpha remove -alpha off
      icon-1024.png` (or flatten in Preview/Export).
- [ ] In Xcode: open `Assets.xcassets` → **AppIcon**. In modern Xcode (single-size), drag the 1024 into
      the **"Any Appearance" / All Sizes** well; Xcode + the build pipeline generate every required size.
      (If your Xcode shows individual slots, use **"Single Size"** in the attributes inspector, or an
      asset generator, so you only provide the 1024.)
- [ ] Optionally add **Dark** and **Tinted** iOS 18 icon variants (extra wells) — a dark-background
      version and a monochrome/tinted version. Nice-to-have, not required.
- [ ] Archive/validate in Xcode Organizer — it will flag alpha-channel or missing-size problems before
      submission.

---

## 5. Splash / launch screen

### 5a. Android 12+ Splash Screen API (single centered icon on solid brand color)
Android 12+ shows a system splash: your **app icon centered on a solid window background**. The icon
must fit the **masked circle safe zone** — a **240×240 dp** window, icon art within the central
**~160 dp** (a windowed icon) or **~192 dp** (full-bleed). Keep the glyph small and centered.

- [ ] Add the `androidx.core:core-splashscreen` dependency and a theme:
      ```xml
      <style name="Theme.App.Starting" parent="Theme.SplashScreen">
          <item name="windowSplashScreenBackground">@color/brand_teal</item> <!-- #0F5E66 -->
          <item name="windowSplashScreenAnimatedIcon">@drawable/ic_splash</item>
          <item name="postSplashScreenTheme">@style/Theme.App</item>
      </style>
      ```
      and `installSplashScreen()` before `setContent {}` in the Activity.
- [ ] `ic_splash` = your **foreground glyph only** (transparent, monochrome-safe or brand color) as a
      **vector drawable**, sized so essential content stays within the center safe circle (it gets
      masked). Reuse `ic_launcher_foreground` if it already fits.
- [ ] Test on Android 12+ that the glyph is centered and **not clipped** by the circle mask.

### 5b. iOS launch screen (storyboard)
- [ ] Use a **LaunchScreen.storyboard** (Apple requires a storyboard, not a static image). Set the view
      background to `#0F5E66` and add a single centered `UIImageView` with the glyph (an asset-catalog
      image), constrained center-X/center-Y, fixed size (~120×120 pt), `contentMode = Aspect Fit`.
- [ ] Keep it **static** (no text, minimal) — Apple rejects launch screens that look like ads/loaders.
- [ ] Ensure the launch screen background matches the first real screen to avoid a flash.

> **KMP note:** this Compose Multiplatform app boots into a native Activity (Android) and the
> `iosApp` Xcode project (iOS), so both native mechanisms above still apply.

---

## 6. Google Play store listing assets

- [ ] **App icon:** 512×512 px, 32-bit PNG, ≤1 MB → `assets/store/play/icon-512.png` (from §2e).
- [ ] **Feature graphic:** **1024×500 px** PNG/JPEG, **no alpha**. Shown at the top of the listing and in
      promos. Keep the mark off-center-left, leave a safe margin (Play may overlay a play button for
      video). **No essential text** (or keep it large + localizable). → `assets/store/play/feature-1024x500.png`.
- [ ] **Phone screenshots:** minimum **2** (up to 8). PNG/JPEG, 16:9 or 9:16, each side **1080–3840 px**.
      Recommended capture size: **1080×1920** or **1080×2400 px** portrait.
- [ ] (Optional) **7-inch tablet** screenshots (e.g. 1200×1920) and **10-inch tablet** (e.g. 1600×2560) —
      required only if you declare tablet support / want tablet featuring.
- [ ] All store text is entered in the Play Console (see §8), not baked into images.

### Feature-graphic background prompt (art only — add any text yourself, or leave textless)
```
A wide horizontal banner background, 1024x500, for a humanitarian earthquake-relief app. Calm flat
minimalist style: a smooth gradient-free deep teal-blue (#0F5E66) field with subtle faint concentric
seismic ripple lines and a soft warm amber (#F4A825) glow on the right third. Plenty of clean negative
space on the left for a logo. Flat 2D, no photorealism, no people, no text, no flags, no clutter, no
harsh red. Landscape 1024x500 aspect ratio.
Do NOT include any text, letters, words, logos, faces, or country symbols.
```

---

## 7. Apple App Store listing assets

- [ ] **App icon:** the **1024×1024** from §4 (uploaded via the asset catalog with the build; no separate
      upload in App Store Connect for modern flow).
- [ ] **Screenshots — required set(s).** App Store requires at least one screenshot for the largest
      supported iPhone. Provide the **6.9"/6.7" iPhone** set at minimum; a 6.9" set can auto-scale to
      smaller devices.

| Device class | Portrait px (accepted) | Notes |
|---|---|---|
| iPhone 6.9" (15/16 Pro Max) | **1290×2796** | Newest largest; also accepts 1320×2868 |
| iPhone 6.7" (14/15 Plus, Pro Max) | **1284×2778** or **1290×2796** | Minimum required class historically |
| iPhone 6.5" (older) | 1242×2688 | Optional |
| iPad 12.9" / 13" | **2048×2732** | Required only if app supports iPad |

- [ ] Provide **3–10** screenshots per required device size (min 1, aim for 3–5 showing key value).
- [ ] Screenshots must have **no alpha** and match the exact device pixel size (App Store Connect is
      strict — wrong dimensions are rejected).

---

## 8. Capturing & framing real screenshots (both stores)

- [ ] Pick a device to capture the **golden** set: Android emulator (e.g. Pixel 8) and iOS simulator
      (iPhone 16 Pro Max for the 6.9" set).
- [ ] Seed good, non-empty data (a strong quake in the feed, several help points on the map, a couple of
      aid-board posts) so screenshots look alive — one per key screen: **Overview**, **Map / help
      points**, **Aid network board**, **Guide**, **SOS**.
- [ ] **Android capture:** `adb exec-out screencap -p > shot.png`, or Android Studio **Running Devices →
      camera** button. Emulator screenshots already match required pixel sizes.
- [ ] **iOS capture:** Simulator **File → Save Screen** (⌘S), or `xcrun simctl io booted screenshot
      shot.png`. Using the exact simulator device gives the exact required resolution.
- [ ] (Optional) **Frame + caption:** overlay device frames and a one-line localized caption using a
      tool like [fastlane frameit](https://docs.fastlane.tools/actions/frameit/),
      [screenshots.pro], [previewed.app], or [AppMockUp]. Keep captions localized per §9 and
      high-contrast/accessible. Do **not** cover essential UI.
- [ ] Export per-locale screenshot sets if the captions differ by language.

---

## 9. Localized store copy + screenshots (per market/locale)

Provide listings in **Spanish** (Colombia + Spain markets), **English**, and **Bahasa Indonesia**
(Indonesia). Fill the templates below and paste into Play Console / App Store Connect per locale.

**Locale codes:** Play — `es-419` (Latin America/Colombia), `es-ES` (Spain), `en-US`, `id-ID`.
App Store — `es-MX`/`es-ES`, `en-US`, `id`. Colombia and Spain can share one Spanish text or split for
regional wording (Colombia `es-419`, Spain `es-ES`).

### Field limits (fill exactly, don't exceed)
| Field | Play limit | App Store limit |
|---|---|---|
| App name / title | 30 chars | 30 chars |
| Subtitle | — | 30 chars |
| Short description | 80 chars | (uses subtitle) |
| Full description | 4000 chars | 4000 chars |
| Keywords | (not a field; woven into description) | 100 chars, comma-separated |
| Promotional text | — | 170 chars (updatable without review) |

### Template — Spanish (`es-419` / `es-ES`)
- [ ] **Título (≤30):** `Kuna Sismo Ayuda`
- [ ] **Subtítulo/Short (≤30 / ≤80):** `Red de ayuda ante sismos` / `Sismos en vivo, refugios, SOS y red de ayuda mutua.`
- [ ] **Descripción completa (≤4000):**
      `Kuna Sismo Ayuda conecta a las comunidades afectadas por terremotos. Consulta sismos recientes
      priorizados por región, encuentra centros de ayuda y refugios oficiales en el mapa, pide ayuda con
      el botón SOS geolocalizado o marca "estoy a salvo", y coordina apoyo en la red de ayuda mutua.
      Disponible para Colombia, Indonesia y España. Accesible y pensado para situaciones de alta
      tensión.`  *(expandir con secciones: Sismos en vivo · Mapa de ayuda · SOS · Guía de emergencia)*
- [ ] **Keywords (App Store, ≤100):** `sismo,terremoto,SOS,emergencia,refugio,ayuda,desastre,rescate,mapa,alerta`
- [ ] **Texto promocional (≤170):** `Ayuda en tiempo real ante terremotos: sismos, refugios y SOS.`

### Template — English (`en-US`)
- [ ] **Title (≤30):** `Kuna Sismo Ayuda`
- [ ] **Subtitle/Short:** `Earthquake help network` / `Live quakes, shelters, SOS and a mutual-aid network.`
- [ ] **Full description (≤4000):**
      `Kuna Sismo Ayuda connects communities affected by earthquakes. See recent quakes prioritized by
      region, find official help points and shelters on the map, call for help with a geolocated SOS
      button or mark yourself safe, and coordinate support in the mutual-aid network. Available for
      Colombia, Indonesia and Spain. Accessible and built for high-stress moments.`
- [ ] **Keywords (≤100):** `earthquake,quake,SOS,emergency,shelter,disaster,relief,rescue,map,alert,aid`
- [ ] **Promo text (≤170):** `Real-time earthquake help: live quakes, shelters and SOS.`

### Template — Bahasa Indonesia (`id-ID`)
- [ ] **Judul (≤30):** `Kuna Sismo Ayuda`
- [ ] **Subjudul/Short:** `Jaringan bantuan gempa` / `Gempa langsung, tempat perlindungan, SOS & bantuan.`
- [ ] **Deskripsi lengkap (≤4000):**
      `Kuna Sismo Ayuda menghubungkan komunitas yang terdampak gempa bumi. Lihat gempa terbaru menurut
      wilayah, temukan titik bantuan dan tempat perlindungan resmi di peta, minta bantuan dengan tombol
      SOS berbasis lokasi atau tandai "saya aman", dan koordinasikan dukungan dalam jaringan bantuan.
      Tersedia untuk Kolombia, Indonesia, dan Spanyol. Aksesibel dan dirancang untuk situasi darurat.`
- [ ] **Kata kunci (≤100):** `gempa,gempa bumi,SOS,darurat,perlindungan,bantuan,bencana,penyelamatan,peta,peringatan`
- [ ] **Teks promosi (≤170):** `Bantuan gempa real-time: gempa langsung, perlindungan, dan SOS.`

- [ ] Upload **localized screenshots** (captions translated) per locale, or reuse one set if captions are
      image-free.

---

## 10. Additional steps you missed (do NOT skip — required to publish)

These are mandatory or high-risk items absent from the starting checklist.

### 10a. Privacy policy URL (both stores REQUIRE it)
- [ ] Publish a **privacy policy** at a public URL (use the planned landing page, or a free host). Both
      Play and App Store require the URL in the listing.
- [ ] The policy MUST disclose: **device location** collected for the SOS feature, **optional contact
      info** (phone/email/name) submitted on aid-board and Lost & Found posts, and any analytics/crash
      data. State retention and that posts are moderated / contact is public only while a post is live.

### 10b. Google Play **Data Safety** form
- [ ] Declare **Location → Approximate/Precise location**: collected, used for **App functionality
      (SOS/emergency)**, **not shared** with third parties (unless it is), user can request deletion.
- [ ] Declare **Personal info → Name, Phone number, Email address**: collected **optionally** for aid-board
      contact, **App functionality**, shared publicly on posts while active (declare "shared" if visible
      to other users). Mark as **optional**.
- [ ] Declare **Photos** (Lost & Found photo uploads) if applicable.
- [ ] State whether data is **encrypted in transit** (yes — HTTPS) and your **deletion** mechanism
      (device-gated resolve / moderator delete).

### 10c. Apple **App Privacy** "nutrition labels" (App Store Connect)
- [ ] **Location (Precise):** Data Used — **App Functionality**; **not** linked to identity if you don't
      store accounts for regular users (the app keeps regular users anonymous), **not** used for tracking.
- [ ] **Contact Info (Name / Phone / Email):** **App Functionality**; optional; declare linkage honestly.
- [ ] **User Content (Photos, posts):** App Functionality.
- [ ] Confirm **"Data is not used to track you"** (no cross-app tracking / ad SDKs).
- [ ] Add the required **`NSLocationWhenInUseUsageDescription`** purpose string in `Info.plist`
      (already present per project notes) and make sure its wording matches the privacy label.

### 10d. Content / age rating questionnaires
- [ ] **Play — IARC content rating:** complete the questionnaire (utility/reference app; no violence;
      note user-generated content = the aid board/posts, which affects the rating and may require a
      content-moderation attestation).
- [ ] **App Store — Age Rating:** complete the questionnaire. Declare **user-generated content** (posts)
      — Apple requires a moderation mechanism + a way to report/block; you have moderation, mention it.
- [ ] Provide a **UGC safeguards** note (report/flag + moderator removal) — both stores scrutinize apps
      with user posts.

### 10e. Maskable / adaptive safe-zone verification (prevents clipped icons)
- [ ] Confirm the **adaptive icon** art keeps essential content within the inner **72×72 dp** of the
      **108×108 dp** layer, and ideally within the **66 dp** circle. Outer 18 dp on each side is
      **crop-only**.
- [ ] Preview across launcher mask shapes (circle, squircle, rounded square, teardrop) in Image Asset
      Studio — nothing important touches the edge.
- [ ] For any web/PWA maskable icon (landing page), keep the glyph within the central **~80%** (maskable
      safe zone).

### 10f. Accessibility / contrast check of the mark
- [ ] Verify the glyph-vs-background contrast is strong (aim WCAG **≥3:1** for the mark, ideally 4.5:1).
      Check with a contrast tool (WebAIM). The teal `#0F5E66` vs white paper `#F7FAFA` passes; verify your
      final pairing.
- [ ] Confirm the mark is **recognizable in pure monochrome** (grayscale it) — required since notification
      + themed icons strip color. If it falls apart in gray, simplify the mark.
- [ ] Confirm legibility at **24×24 px** (notification) and **48 px** (mdpi launcher).

### 10g. Master source + non-destructive export structure
- [ ] Keep a **vector/SVG master** (`assets/source/mark-master.svg`) + the cleaned large PNG. Never
      re-generate from scratch for each size — always export from the master.
- [ ] Use this folder layout (create under `assets/`, keep out of `res/`; commit source separately):
      ```
      assets/
        source/            # masters — SVG + cleaned large PNGs, layered files
          mark-master.svg
          mark-master.png              # transparent, 2048+
          ic_launcher_foreground.png
          ic_launcher_monochrome.png
          ic_stat_notify.png           # white silhouette
        store/
          play/   icon-512.png  feature-1024x500.png  screenshots/{es-419,es-ES,en-US,id-ID}/
          appstore/ icon-1024.png  screenshots/{6.9,6.7,ipad}/
        splash/  ic_splash.(svg|png)
        web/     favicon/ ...
      ```
- [ ] Adopt consistent **file naming**: lowercase, hyphen/underscore, size-suffixed
      (`icon-512.png`, `feature-1024x500.png`, `screenshot-overview-en-1290x2796.png`).

### 10h. Landing-page favicon (nice-to-have — a landing page IS planned)
- [ ] From the master, export favicons: `favicon.ico` (16/32/48 multi-size), `favicon-32.png`,
      `apple-touch-icon.png` (180×180, solid background, no transparency), `icon-192.png` + `icon-512.png`
      (PWA maskable), and a `site.webmanifest`.
- [ ] Use the **simple mark on a solid brand square** (favicons are tiny — detail disappears).
- [ ] Generate via [realfavicongenerator.net](https://realfavicongenerator.net) from `mark-master.png`.

---

## 11. Contrast & monochrome quick-check (gate before exporting sizes)
- [ ] Grayscale the chosen mark → still readable? If no, simplify.
- [ ] Mark vs background ≥3:1 contrast (tool-verified).
- [ ] Shrink to 24 px → still identifiable?
- [ ] No reliance on color alone to convey the concept.

---

## 12. Final pre-submission verification checklist

**Icons**
- [ ] Android adaptive icon: fg + bg + **monochrome** all present; nothing clipped by any mask.
- [ ] Play 512×512 PNG, ≤1 MB, flat (no transparency needed).
- [ ] iOS 1024×1024, sRGB, **no alpha**, no rounded corners, no shadow — validates in Xcode Organizer.
- [ ] Notification icon = white/transparent silhouette; real push shows a crisp glyph (no gray blob).
- [ ] Themed icon tested on Android 13+.

**Splash**
- [ ] Android 12+ splash: glyph centered, within circle safe zone, brand background.
- [ ] iOS LaunchScreen.storyboard: centered glyph, matching background, no text/loader.

**Store**
- [ ] Play: icon-512, feature-1024×500, ≥2 phone screenshots per locale.
- [ ] App Store: 1024 icon + 6.9"/6.7" screenshots at exact pixel sizes (no alpha).
- [ ] Localized listings filled for `es-419`/`es-ES`, `en-US`, `id-ID` within char limits.

**Compliance**
- [ ] Privacy policy URL live and linked in both listings.
- [ ] Play Data Safety form completed (location + optional contact + photos).
- [ ] Apple App Privacy labels completed and consistent with `Info.plist` purpose strings.
- [ ] Content/age ratings (IARC + App Store) completed, UGC/moderation declared.

**Sources**
- [ ] Master SVG + large PNGs committed under `assets/source/`; export folder structured; consistent
      naming.
- [ ] One brand concept used consistently across every asset.
