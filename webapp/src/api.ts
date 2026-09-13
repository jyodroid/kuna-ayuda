// Public API client for the Kuna web app. Same-origin with the server (served at /app), so it calls
// /api/** directly — all reads here are public (no auth). It only attaches the app-only key, exactly
// like the console and the mobile apps: a deterrent that filters casual scanners/bots, NOT a secret
// (it ships in the bundle). See server config/AppGate.kt.

import type { CountryCode } from "./content/countries";

const APP_KEY = "b7e2d40915a86c3f0e1d7942bc63f58a2049e1cd76b8340af5921e6d0c8b47f3";

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
  }
}

async function api<T>(path: string): Promise<T> {
  const res = await fetch(path, { headers: { "X-App-Key": APP_KEY } });
  if (!res.ok) {
    let message = `Error ${res.status}`;
    try {
      const j = await res.json();
      message = j?.error?.message ?? j?.message ?? j?.error ?? message;
    } catch {
      /* ignore */
    }
    throw new ApiError(res.status, message);
  }
  if (res.status === 204) return undefined as T;
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

function qs(params: Record<string, string | number | undefined>): string {
  const p = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) if (v !== undefined && v !== "") p.set(k, String(v));
  const s = p.toString();
  return s ? `?${s}` : "";
}

// ---- types (mirror the server route DTOs) -----------------------------------------------------

export interface Quake {
  id: string;
  time: number; // epoch ms UTC
  magnitude: number | null;
  depthKm: number | null;
  latitude: number;
  longitude: number;
  place: string;
  source: string;
  url: string | null;
}

export interface Fire {
  id: string;
  time: number;
  latitude: number;
  longitude: number;
  brightnessK: number | null;
  frpMw: number | null;
  confidence: string | null;
  daynight: string | null;
  source: string;
  place: string | null;
}

export interface Shelter {
  id: number;
  name: string;
  type: string;
  address: string;
  latitude: number;
  longitude: number;
  accepts: string;
  hours: string | null;
  contactPhone: string | null;
  verified: boolean;
  lastVerified: string | null;
}

export interface BoardPost {
  id: number;
  kind: string;
  resourceType: string;
  region: string;
  description: string;
  contactPhone: string | null;
  contactEmail: string | null;
  contactName: string | null;
  status: string;
  country: string;
  createdAt: string;
  collectionPoints?: { name: string; address: string; hours: string }[];
}

export interface SearchReport {
  id: number;
  subject: string;
  state: string;
  title: string;
  description: string;
  lastSeen: string;
  notes: string | null;
  contactPhone: string;
  contactName: string | null;
  photoId: number | null;
  country: string;
  status: string;
  createdAt: string;
}

export interface SafeCheckIn {
  id: number;
  name: string;
  region: string | null;
  createdAtEpochMs: number;
}

export interface GuideContact {
  category: string;
  name: string;
  phone: string;
}
export interface TipStep {
  img: string;
  caption: string;
}
export interface GuideTip {
  id: string;
  title: string;
  body: string;
  steps: TipStep[];
}
export interface TipPhase {
  key: string;
  label: string;
  tips: GuideTip[];
}
export interface Guide {
  country: string;
  lang: string;
  emergency: { general: string; contacts: GuideContact[] };
  tips: TipPhase[];
}

// ---- endpoints (all public GET, ?country= filtered) -------------------------------------------

export const kuna = {
  quakes: (country: CountryCode, minMagnitude?: number) =>
    api<Quake[]>(`/api/quakes${qs({ country, minMagnitude })}`),
  fires: (country: CountryCode) => api<Fire[]>(`/api/fires${qs({ country })}`),
  shelters: (country: CountryCode) => api<Shelter[]>(`/api/shelters${qs({ country })}`),
  board: (country: CountryCode, kind?: string, type?: string, region?: string) =>
    api<BoardPost[]>(`/api/board${qs({ country, kind, type, region })}`),
  search: (country: CountryCode, subject?: string, state?: string) =>
    api<SearchReport[]>(`/api/search${qs({ country, subject, state })}`),
  safe: (country: CountryCode) => api<SafeCheckIn[]>(`/api/sos/safe${qs({ country })}`),
  guide: (country: CountryCode, lang: string) => api<Guide>(`/api/guide${qs({ country, lang })}`),
  photoUrl: (id: number) => `/api/photos/${id}`,
};

// Fetch a photo WITH the X-App-Key header (an <img src> can't set headers, so when the app-gate is on
// a plain URL would 401). Returns an object URL the caller must revoke.
export async function photoObjectUrl(id: number): Promise<string> {
  const res = await fetch(`/api/photos/${id}`, { headers: { "X-App-Key": APP_KEY } });
  if (!res.ok) throw new ApiError(res.status, `photo ${id}`);
  return URL.createObjectURL(await res.blob());
}
