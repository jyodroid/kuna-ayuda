// Human place label for a fire, mirroring the app (ui/fires FireUi.firePlaceLabel): use the fire's own
// place when present (GDACS); otherwise the NEAREST known city — "Cerca de X" (≤40 km), "A N km de X"
// (≤500 km), else the coordinates so it's never "unknown". FIRMS points carry no place, so this is what
// turns raw "-0.08, -77.66" rows into readable locations.

import type { Fire } from "../api";
import { CITIES, type City } from "../content/cities";
import type { CountryCode } from "../content/countries";
import type { Lang } from "../i18n";
import { distanceKm } from "./severity";

export interface NearCity {
  city: City;
  km: number;
}

export function nearestCity(lat: number, lon: number, country: CountryCode): NearCity | null {
  const list = CITIES[country];
  if (!list || list.length === 0) return null;
  let best: City | null = null;
  let bestKm = Infinity;
  for (const c of list) {
    const km = distanceKm(lat, lon, c.lat, c.lon);
    if (km < bestKm) {
      bestKm = km;
      best = c;
    }
  }
  return best ? { city: best, km: bestKm } : null;
}

// Compact coordinates, e.g. "3.9°S, 67.5°W" (cardinal letters are language-neutral).
export function fireCoords(lat: number, lon: number): string {
  const deg = (v: number, pos: string, neg: string) => {
    const whole = Math.round(Math.abs(v) * 10);
    return `${Math.floor(whole / 10)}.${whole % 10}°${v >= 0 ? pos : neg}`;
  };
  return `${deg(lat, "N", "S")}, ${deg(lon, "E", "W")}`;
}

export function firePlaceLabel(fire: Fire, country: CountryCode, lang: Lang): string {
  if (fire.place && fire.place.trim()) return fire.place;
  const near = nearestCity(fire.latitude, fire.longitude, country);
  if (near) {
    if (near.km <= 40) return lang === "es" ? `Cerca de ${near.city.name}` : `Near ${near.city.name}`;
    if (near.km <= 500) {
      const km = Math.round(near.km);
      return lang === "es" ? `A ${km} km de ${near.city.name}` : `${km} km from ${near.city.name}`;
    }
  }
  return fireCoords(fire.latitude, fire.longitude);
}
