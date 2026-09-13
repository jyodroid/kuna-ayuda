// Unified point model the map renders. The list/detail read the raw records; the map only needs
// position + severity (color) + an icon (type). Icon carries the TYPE, color carries the SEVERITY —
// so the two are never confused (the old red/orange-bullet problem), and both are in the legend.

import type { Fire, Quake, Shelter } from "../api";
import { fireSeverity, quakeSeverity, type Severity } from "./severity";

export type HazardKind = "quake" | "fire" | "shelter";

export interface MapPoint {
  id: string; // globally unique across layers, e.g. "quake:us7000..."
  kind: HazardKind;
  lat: number;
  lon: number;
  severity: Severity;
  icon: string; // emoji glyph
}

export const KIND_ICON: Record<HazardKind, string> = {
  quake: "⛰️",
  fire: "🔥",
  shelter: "🏠",
};

// Shelter icon by type (used from W4).
export function shelterIcon(type: string): string {
  switch (type.toUpperCase()) {
    case "ACOPIO":
      return "📦";
    case "ALBERGUE":
      return "🏠";
    case "SALUD":
      return "🏥";
    case "AGUA":
      return "💧";
    default:
      return "📍";
  }
}

export function quakePoint(q: Quake): MapPoint {
  return {
    id: `quake:${q.id}`,
    kind: "quake",
    lat: q.latitude,
    lon: q.longitude,
    severity: quakeSeverity(q.magnitude),
    icon: KIND_ICON.quake,
  };
}

export function firePoint(f: Fire): MapPoint {
  return {
    id: `fire:${f.id}`,
    kind: "fire",
    lat: f.latitude,
    lon: f.longitude,
    severity: fireSeverity(f),
    icon: KIND_ICON.fire,
  };
}

export function shelterPoint(s: Shelter): MapPoint {
  return {
    id: `shelter:${s.id}`,
    kind: "shelter",
    lat: s.latitude,
    lon: s.longitude,
    severity: "info",
    icon: shelterIcon(s.type),
  };
}
