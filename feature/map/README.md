# :feature:map

Cross-platform earthquake map, backed by [MapLibre Compose](https://maplibre.org/maplibre-compose/)
(`org.maplibre.compose:maplibre-compose`).

- **`commonMain`** — `expect fun QuakeMap(quakes, onMarkerTap, modifier)`.
- **`mobileMain`** (shared Android + iOS) — the real MapLibre implementation: a `MaplibreMap` with a
  `CircleLayer` of quake epicenters. Each epicenter carries its quake id in the feature's
  `properties`, and `CircleLayer.onClick` reads it back so tapping a marker opens exactly that
  quake. MapLibre is a dependency only here, so it never leaks into the desktop target.
- **`jvmMain`** — a placeholder (MapLibre's desktop runtime needs a Java 25 toolchain; enabling it
  is a separate, deliberate task).

## iOS: one-time native setup (required before the iOS app can link)

The Kotlin side is complete, but MapLibre Native must be linked into the Xcode app. In
`iosApp/iosApp.xcodeproj`:

1. **File → Add Package Dependencies…**
2. Enter `https://github.com/maplibre/maplibre-gl-native-distribution.git`
3. Pin the version to **`6.25.1`** (the version MapLibre Compose 0.14.0 targets) and add the
   `MapLibre` product to the `iosApp` target.
4. **Linker order is already handled**: `iosApp/Configuration/Config.xcconfig` sets
   `OTHER_LDFLAGS = $(inherited) -framework ComposeApp -framework MapLibre`, which forces the required
   order (ComposeApp *before* MapLibre — both bundle HarfBuzz; the reverse order silently breaks
   Compose text rendering on iOS). You don't need to reorder *Link Binary With Libraries* by hand.
5. (ATS) For local dev over `http://localhost:8080`, allow local networking in `Info.plist` if the
   quake feed is blocked.

**If the link still fails after adding the package**, the other two lines in the error are secondary,
not the MapLibre problem:
- `libicu…was built for newer 'iOS-simulator' version (18.5) than being linked (18.2)` — a **warning**
  from an SDK skew between the Gradle-built `ComposeApp.framework` and the app's deployment target
  (`IPHONEOS_DEPLOYMENT_TARGET = 18.2`). Harmless; to silence it, align both to the same simulator SDK.
- `cannot link directly with 'SwiftUICore'` — collateral from the *failed* link; it clears once
  MapLibre resolves. If it persists on its own, it's the same SDK-alignment issue, not MapLibre.

CocoaPods is an alternative to SPM (`pod("MapLibre", "6.25.1")`) if you switch the iOS integration
to Pods — but this project uses the plain Xcode + SPM path.

> Not buildable in a headless/CI environment without Xcode + a Mac. The `mobileMain` source it
> compiles is identical to the Android target, which builds and is verified.
