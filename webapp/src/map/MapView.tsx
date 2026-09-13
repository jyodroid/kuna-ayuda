import { useEffect, useRef } from "react";
import maplibregl from "maplibre-gl";
import { SEVERITY_COLOR } from "./severity";
import type { MapPoint } from "./points";

// OpenFreeMap "Liberty" vector tiles — same free, no-key base map the mobile app uses
// (feature/map DisasterMap.mobile.kt). Set a real style or MapLibre falls back to a label-less demo.
const STYLE_URI = "https://tiles.openfreemap.org/styles/liberty";

interface Props {
  points: MapPoint[];
  selectedId: string | null;
  onSelect: (id: string) => void;
  focus: { lat: number; lon: number; zoom?: number } | null;
  userLoc: { lat: number; lon: number } | null;
  initialCenter: { lat: number; lon: number; zoom: number };
}

export function MapView({ points, selectedId, onSelect, focus, userLoc, initialCenter }: Props) {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const mapRef = useRef<maplibregl.Map | null>(null);
  const markersRef = useRef<Map<string, { marker: maplibregl.Marker; el: HTMLElement }>>(new Map());
  const userMarkerRef = useRef<maplibregl.Marker | null>(null);
  const readyRef = useRef(false);

  // Init the map once.
  useEffect(() => {
    if (mapRef.current || !containerRef.current) return;
    const map = new maplibregl.Map({
      container: containerRef.current,
      style: STYLE_URI,
      center: [initialCenter.lon, initialCenter.lat],
      zoom: initialCenter.zoom,
      attributionControl: { compact: true },
    });
    map.addControl(new maplibregl.NavigationControl({ showCompass: false }), "top-right");
    map.on("load", () => {
      readyRef.current = true;
    });
    mapRef.current = map;
    return () => {
      map.remove();
      mapRef.current = null;
      markersRef.current.clear();
      readyRef.current = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Sync markers whenever the point set changes.
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    const existing = markersRef.current;
    const nextIds = new Set(points.map((p) => p.id));

    // Remove markers no longer present.
    for (const [id, { marker }] of existing) {
      if (!nextIds.has(id)) {
        marker.remove();
        existing.delete(id);
      }
    }
    // Add new markers.
    for (const p of points) {
      if (existing.has(id(p))) continue;
      const el = document.createElement("button");
      el.type = "button";
      el.className = "kuna-marker";
      el.style.setProperty("--sev", SEVERITY_COLOR[p.severity]);
      el.textContent = p.icon;
      el.setAttribute("aria-label", p.id);
      el.addEventListener("click", (e) => {
        e.stopPropagation();
        onSelect(p.id);
      });
      const marker = new maplibregl.Marker({ element: el }).setLngLat([p.lon, p.lat]).addTo(map);
      existing.set(p.id, { marker, el });
    }
    function id(p: MapPoint) {
      return p.id;
    }
  }, [points, onSelect]);

  // Reflect selection (scale the chosen marker).
  useEffect(() => {
    for (const [pid, { el }] of markersRef.current) {
      el.classList.toggle("kuna-marker--on", pid === selectedId);
    }
  }, [selectedId, points]);

  // Fly to the current focus (selected point, near-me, or country recenter).
  useEffect(() => {
    const map = mapRef.current;
    if (!map || !focus) return;
    map.flyTo({ center: [focus.lon, focus.lat], zoom: focus.zoom ?? map.getZoom(), essential: true });
  }, [focus]);

  // User-location dot.
  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    if (!userLoc) {
      userMarkerRef.current?.remove();
      userMarkerRef.current = null;
      return;
    }
    if (!userMarkerRef.current) {
      const el = document.createElement("div");
      el.className = "kuna-userdot";
      userMarkerRef.current = new maplibregl.Marker({ element: el });
    }
    userMarkerRef.current.setLngLat([userLoc.lon, userLoc.lat]).addTo(map);
  }, [userLoc]);

  return <div ref={containerRef} className="h-full w-full" />;
}
