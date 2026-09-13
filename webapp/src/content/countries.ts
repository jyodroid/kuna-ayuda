// Supported countries — ported from the app's `Country` enum (core/domain Country.kt). The web map
// centers on `centerLat/centerLon/zoom` when the user's location is unavailable. `code` drives the
// server's `?country=` filter (quakes/fires/shelters/board/search/sos-safe/guide).

export type CountryCode = "CO" | "ID" | "ES" | "IT" | "PE";

export interface Country {
  code: CountryCode;
  nameEs: string;
  nameEn: string;
  flag: string;
  centerLat: number;
  centerLon: number;
  zoom: number;
}

export const COUNTRIES: Country[] = [
  { code: "CO", nameEs: "Colombia", nameEn: "Colombia", flag: "🇨🇴", centerLat: 4.6, centerLon: -74.1, zoom: 5 },
  { code: "ID", nameEs: "Indonesia", nameEn: "Indonesia", flag: "🇮🇩", centerLat: -2.5, centerLon: 118.0, zoom: 4 },
  { code: "ES", nameEs: "España", nameEn: "Spain", flag: "🇪🇸", centerLat: 40.0, centerLon: -3.7, zoom: 5 },
  { code: "IT", nameEs: "Italia", nameEn: "Italy", flag: "🇮🇹", centerLat: 42.5, centerLon: 12.5, zoom: 5 },
  { code: "PE", nameEs: "Perú", nameEn: "Peru", flag: "🇵🇪", centerLat: -9.2, centerLon: -75.0, zoom: 5 },
];

export const DEFAULT_COUNTRY: CountryCode = "CO";

export function countryOf(code: string | null | undefined): Country {
  return COUNTRIES.find((c) => c.code === code) ?? COUNTRIES[0];
}
