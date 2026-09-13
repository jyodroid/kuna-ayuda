import { useCallback, useState } from "react";

// Browser geolocation, on demand. Never blocks: a 10s timeout resolves to "denied" so the map always
// falls back to the country centroid. Mirrors the app's on-demand location (core:location).

export type GeoStatus = "idle" | "requesting" | "granted" | "denied";

export interface GeoState {
  status: GeoStatus;
  loc: { lat: number; lon: number } | null;
  request: () => void;
}

export function useGeolocation(): GeoState {
  const [status, setStatus] = useState<GeoStatus>("idle");
  const [loc, setLoc] = useState<{ lat: number; lon: number } | null>(null);

  const request = useCallback(() => {
    if (!("geolocation" in navigator)) {
      setStatus("denied");
      return;
    }
    setStatus("requesting");
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLoc({ lat: pos.coords.latitude, lon: pos.coords.longitude });
        setStatus("granted");
      },
      () => setStatus("denied"),
      { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 },
    );
  }, []);

  return { status, loc, request };
}
