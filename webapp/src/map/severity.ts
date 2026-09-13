// Severity + labeling helpers, mirroring the mobile app so the web matches exactly:
//  - quake magnitude bands  → ui/quakes/QuakeUi.kt (<3 light, <4.5 moderate, <6 strong, ≥6 major)
//  - fire intensity         → core/domain Fire.kt (frpMw ≥100 HIGH, ≥20 MODERATE, else LOW;
//                             GDACS confidence red/orange/else)
// Color is ALWAYS paired with the text label below (accessibility: never color alone).

import type { Fire, Quake } from "../api";

export type Severity = "info" | "low" | "moderate" | "high";

export interface Bilingual {
  es: string;
  en: string;
}

// Map colors for the severity ring (hex). Reserved for severity only.
export const SEVERITY_COLOR: Record<Severity, string> = {
  info: "#0F5E66", // brand teal — neutral/help
  low: "#2E7D32", // green
  moderate: "#EF6C00", // orange
  high: "#C62828", // red
};

// ---- quakes -----------------------------------------------------------------------------------

export function quakeSeverity(mag: number | null): Severity {
  if (mag == null) return "info";
  if (mag < 3.0) return "low";
  if (mag < 4.5) return "moderate";
  return "high"; // <6 strong, ≥6 major both read as high on the map
}

export function quakeSeverityLabel(mag: number | null): Bilingual {
  if (mag == null) return { es: "Magnitud desconocida", en: "Unknown magnitude" };
  if (mag < 3.0) return { es: "Leve", en: "Light" };
  if (mag < 4.5) return { es: "Moderado", en: "Moderate" };
  if (mag < 6.0) return { es: "Fuerte", en: "Strong" };
  return { es: "Mayor", en: "Major" };
}

// ---- fires ------------------------------------------------------------------------------------

export function fireSeverity(f: Pick<Fire, "frpMw" | "confidence">): Severity {
  if (f.frpMw != null) {
    if (f.frpMw >= 100) return "high";
    if (f.frpMw >= 20) return "moderate";
    return "low";
  }
  const c = (f.confidence ?? "").toLowerCase();
  if (c === "red") return "high";
  if (c === "orange") return "moderate";
  return "low";
}

export function fireIntensityLabel(f: Pick<Fire, "frpMw" | "confidence">): Bilingual {
  switch (fireSeverity(f)) {
    case "high":
      return { es: "Intensidad alta", en: "High intensity" };
    case "moderate":
      return { es: "Intensidad moderada", en: "Moderate intensity" };
    default:
      return { es: "Intensidad baja", en: "Low intensity" };
  }
}

// A numeric weight for the heatmap / ranking (W3). frpMw when present, else a confidence proxy.
export function fireWeight(f: Pick<Fire, "frpMw" | "confidence">): number {
  if (f.frpMw != null) return f.frpMw;
  const c = (f.confidence ?? "").toLowerCase();
  if (c === "red") return 120;
  if (c === "orange") return 40;
  return 10;
}

// ---- relative time (mirror core/domain RelativeTime.relativeAgo) --------------------------------

export function relativeAgo(epochMs: number, lang: "es" | "en"): string {
  const diffMin = Math.max(0, Math.floor((Date.now() - epochMs) / 60000));
  if (diffMin < 1) return lang === "es" ? "ahora" : "just now";
  if (diffMin < 60) return lang === "es" ? `hace ${diffMin} min` : `${diffMin} min ago`;
  const h = Math.floor(diffMin / 60);
  if (h < 24) return lang === "es" ? `hace ${h} h` : `${h}h ago`;
  const d = Math.floor(h / 24);
  return lang === "es" ? `hace ${d} d` : `${d}d ago`;
}

// Distance between two lat/lon points in km (haversine — mirror core/domain Geo.distanceKm).
export function distanceKm(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lon2 - lon1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) * Math.cos((lat2 * Math.PI) / 180) * Math.sin(dLon / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}

// Quake helpers that need the raw record.
export function quakeTitle(q: Quake): string {
  const m = q.magnitude != null ? `M${q.magnitude.toFixed(1)}` : "M?";
  return `${m} · ${q.place}`;
}
