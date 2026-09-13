import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { kuna, type Fire, type Quake } from "../api";
import { countryOf, type CountryCode } from "../content/countries";
import { useCountry } from "../state";
import { useI18n } from "../i18n";
import { MapView } from "../map/MapView";
import { firePoint, quakePoint, type HazardKind, type MapPoint } from "../map/points";
import { firePlaceLabel } from "../map/firePlace";
import { useGeolocation } from "../map/useGeolocation";
import { Charts } from "./Charts";
import {
  distanceKm,
  fireIntensityLabel,
  fireSeverity,
  quakeSeverity,
  quakeSeverityLabel,
  quakeTitle,
  relativeAgo,
  SEVERITY_COLOR,
  type Bilingual,
  type Severity,
} from "../map/severity";

// A unified row shown in the linked list, built from quakes + fires.
interface Row {
  id: string;
  kind: HazardKind;
  icon: string;
  lat: number;
  lon: number;
  title: string;
  sub: string;
  severity: Severity;
  severityLabel: Bilingual;
  timeMs: number;
}

type Selected = { kind: "quake"; quake: Quake } | { kind: "fire"; fire: Fire };

const SEV_RANK: Record<Severity, number> = { high: 3, moderate: 2, low: 1, info: 0 };

export function PanelPage() {
  const { t, lang } = useI18n();
  const { country } = useCountry();
  const geo = useGeolocation();

  const [quakes, setQuakes] = useState<Quake[]>([]);
  const [fires, setFires] = useState<Fire[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const [showQuakes, setShowQuakes] = useState(true);
  const [showFires, setShowFires] = useState(true);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [focus, setFocus] = useState<{ lat: number; lon: number; zoom?: number } | null>(null);
  // "Cerca de mí" vs country view. Auto-enabled once when location is first granted (near-me default),
  // but toggleable so the user can always go back to the whole-country view.
  const [nearMode, setNearMode] = useState(false);
  const autoNearRef = useRef(false);

  const c = countryOf(country);
  const nearActive = nearMode && geo.status === "granted" && geo.loc != null;

  // Request the user's location once on mount (for the near-me default).
  useEffect(() => {
    geo.request();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Turn near-me on automatically the first time location is granted (the default), but only once —
  // afterwards it's fully under the toggle's control.
  useEffect(() => {
    if (geo.status === "granted" && geo.loc && !autoNearRef.current) {
      autoNearRef.current = true;
      setNearMode(true);
    }
  }, [geo.status, geo.loc]);

  // Fetch hazards for the selected country.
  const load = useCallback(async () => {
    setLoading(true);
    setError(false);
    try {
      const [q, f] = await Promise.all([kuna.quakes(country), kuna.fires(country)]);
      setQuakes(q);
      setFires(f);
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [country]);

  useEffect(() => {
    setSelectedId(null);
    setNearMode(false); // switching country returns to the whole-country view
    load();
  }, [load]);

  // Camera: user location when near-me is active, else the country centroid. Re-centers on toggle,
  // location change, and country change.
  useEffect(() => {
    if (nearActive && geo.loc) {
      setFocus({ lat: geo.loc.lat, lon: geo.loc.lon, zoom: 9 });
    } else {
      setFocus({ lat: c.centerLat, lon: c.centerLon, zoom: c.zoom });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [nearActive, geo.loc, country]);

  const points = useMemo<MapPoint[]>(() => {
    const pts: MapPoint[] = [];
    if (showQuakes) pts.push(...quakes.map(quakePoint));
    if (showFires) pts.push(...fires.map(firePoint));
    return pts;
  }, [quakes, fires, showQuakes, showFires]);

  const rows = useMemo<Row[]>(() => {
    const list: Row[] = [];
    if (showQuakes) {
      for (const q of quakes) {
        list.push({
          id: `quake:${q.id}`,
          kind: "quake",
          icon: "⛰️",
          lat: q.latitude,
          lon: q.longitude,
          title: quakeTitle(q),
          sub:
            q.depthKm != null
              ? `${q.depthKm.toFixed(0)} ${t("km_deep")} · ${q.source}`
              : q.source,
          severity: quakeSeverity(q.magnitude),
          severityLabel: quakeSeverityLabel(q.magnitude),
          timeMs: q.time,
        });
      }
    }
    if (showFires) {
      for (const f of fires) {
        list.push({
          id: `fire:${f.id}`,
          kind: "fire",
          icon: "🔥",
          lat: f.latitude,
          lon: f.longitude,
          title: firePlaceLabel(f, country, lang),
          sub: f.frpMw != null ? `${f.frpMw.toFixed(0)} MW · ${f.source}` : f.source,
          severity: fireSeverity(f),
          severityLabel: fireIntensityLabel(f),
          timeMs: f.time,
        });
      }
    }
    const u = nearActive ? geo.loc : null;
    list.sort((a, b) => {
      if (u) return dist(u, a) - dist(u, b);
      if (SEV_RANK[b.severity] !== SEV_RANK[a.severity]) return SEV_RANK[b.severity] - SEV_RANK[a.severity];
      return b.timeMs - a.timeMs;
    });
    return list;
  }, [quakes, fires, showQuakes, showFires, nearActive, geo.loc, t, country, lang]);

  const userLoc = nearActive ? geo.loc : null;

  // Raw record behind the current selection, for the detail card.
  const selected = useMemo<Selected | null>(() => {
    if (!selectedId) return null;
    if (selectedId.startsWith("quake:")) {
      const q = quakes.find((x) => `quake:${x.id}` === selectedId);
      return q ? { kind: "quake", quake: q } : null;
    }
    const f = fires.find((x) => `fire:${x.id}` === selectedId);
    return f ? { kind: "fire", fire: f } : null;
  }, [selectedId, quakes, fires]);

  const selectRow = useCallback((r: Row) => {
    setSelectedId(r.id);
    setFocus({ lat: r.lat, lon: r.lon, zoom: 11 });
  }, []);

  return (
    <div className="mx-auto max-w-6xl px-3 py-3">
     <div className="flex flex-col gap-3 md:h-[70vh] md:flex-row">
      {/* Map */}
      <div className="relative h-[45vh] overflow-hidden rounded-lg border border-neutral-200 md:h-full md:flex-1">
        <MapView
          points={points}
          selectedId={selectedId}
          onSelect={(id) => {
            const r = rows.find((x) => x.id === id);
            if (r) selectRow(r);
          }}
          focus={focus}
          userLoc={userLoc}
          initialCenter={{ lat: c.centerLat, lon: c.centerLon, zoom: c.zoom }}
        />
        <div className="absolute left-3 top-3 flex overflow-hidden rounded-full bg-white text-sm font-medium shadow ring-1 ring-neutral-200">
          <button
            type="button"
            aria-pressed={nearActive}
            onClick={() => {
              if (geo.status === "granted") setNearMode(true);
              else geo.request();
            }}
            className={`px-3 py-1.5 ${nearActive ? "bg-primary text-white" : "hover:bg-neutral-50"}`}
          >
            📍 {t("near_me")}
          </button>
          <button
            type="button"
            aria-pressed={!nearActive}
            onClick={() => setNearMode(false)}
            className={`px-3 py-1.5 ${!nearActive ? "bg-primary text-white" : "hover:bg-neutral-50"}`}
          >
            {c.flag} {t("country_view")}
          </button>
        </div>
        {selected ? (
          <DetailCard
            selected={selected}
            country={country}
            onClose={() => setSelectedId(null)}
          />
        ) : (
          <Legend />
        )}
      </div>

      {/* Linked list */}
      <div className="flex min-h-0 flex-col md:w-96">
        <div className="mb-2 flex flex-wrap items-center gap-2">
          <LayerChip on={showQuakes} onClick={() => setShowQuakes((v) => !v)} icon="⛰️" label={t("layer_quakes")} count={quakes.length} />
          <LayerChip on={showFires} onClick={() => setShowFires((v) => !v)} icon="🔥" label={t("layer_fires")} count={fires.length} />
        </div>

        {geo.status === "denied" && (
          <p className="mb-2 rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-800">{t("near_me_denied")}</p>
        )}

        <div className="min-h-0 flex-1 overflow-y-auto rounded-lg border border-neutral-200 bg-white">
          {loading ? (
            <p className="p-4 text-sm text-neutral-500" aria-live="polite">{t("loading")}</p>
          ) : error ? (
            <div className="p-4 text-sm">
              <p className="mb-2 text-neutral-600">{t("load_error")}</p>
              <button className="btn-primary" onClick={load}>{t("retry")}</button>
            </div>
          ) : rows.length === 0 ? (
            <p className="p-4 text-sm text-neutral-500">{t("empty")}</p>
          ) : (
            <ul>
              {rows.map((r) => (
                <RowItem
                  key={r.id}
                  row={r}
                  selected={r.id === selectedId}
                  distanceKm={userLoc ? dist(userLoc, r) : null}
                  onClick={() => selectRow(r)}
                />
              ))}
            </ul>
          )}
        </div>
      </div>
     </div>

      <Charts quakes={quakes} fires={fires} country={country} />
      <p className="mt-3 text-center text-xs text-neutral-400">{t("panel_note")}</p>
    </div>
  );
}

function dist(u: { lat: number; lon: number }, r: { lat: number; lon: number }): number {
  return distanceKm(u.lat, u.lon, r.lat, r.lon);
}

function LayerChip({ on, onClick, icon, label, count }: { on: boolean; onClick: () => void; icon: string; label: string; count: number }) {
  return (
    <button type="button" onClick={onClick} aria-pressed={on} className={`chip ${on ? "chip-on" : "opacity-60"}`}>
      <span aria-hidden>{icon}</span> {label} <span className="text-xs text-neutral-500">({count})</span>
    </button>
  );
}

function RowItem({ row, selected, distanceKm: d, onClick }: { row: Row; selected: boolean; distanceKm: number | null; onClick: () => void }) {
  const { lang } = useI18n();
  return (
    <li>
      <button
        type="button"
        onClick={onClick}
        aria-current={selected}
        className={`flex w-full items-start gap-2 border-b border-neutral-100 px-3 py-2 text-left hover:bg-neutral-50 ${selected ? "bg-primary-muted" : ""}`}
      >
        <span aria-hidden className="mt-0.5 text-lg">{row.kind === "quake" ? "⛰️" : "🔥"}</span>
        <span className="min-w-0 flex-1">
          <span className="block truncate text-sm font-medium text-neutral-800">{row.title}</span>
          <span className="mt-0.5 flex flex-wrap items-center gap-x-2 text-xs text-neutral-500">
            <span className="inline-flex items-center gap-1">
              <span className="inline-block h-2 w-2 rounded-full" style={{ background: SEVERITY_COLOR[row.severity] }} aria-hidden />
              {row.severityLabel[lang]}
            </span>
            <span>· {relativeAgo(row.timeMs, lang)}</span>
            {d != null && <span>· {lang === "es" ? `a ${d.toFixed(0)} km` : `${d.toFixed(0)} km away`}</span>}
          </span>
          <span className="mt-0.5 block truncate text-xs text-neutral-400">{row.sub}</span>
        </span>
      </button>
    </li>
  );
}

function DetailCard({
  selected,
  country,
  onClose,
}: {
  selected: { kind: "quake"; quake: Quake } | { kind: "fire"; fire: Fire };
  country: import("../content/countries").CountryCode;
  onClose: () => void;
}) {
  const { t, lang } = useI18n();
  const isQuake = selected.kind === "quake";
  const timeMs = isQuake ? selected.quake.time : selected.fire.time;
  const sev = isQuake ? quakeSeverity(selected.quake.magnitude) : fireSeverity(selected.fire);
  const sevLabel = isQuake ? quakeSeverityLabel(selected.quake.magnitude) : fireIntensityLabel(selected.fire);
  const title = isQuake ? quakeTitle(selected.quake) : firePlaceLabel(selected.fire, country, lang);
  const absTime = new Date(timeMs).toLocaleString(lang === "es" ? "es" : "en", {
    dateStyle: "medium",
    timeStyle: "short",
  });

  return (
    <div className="absolute bottom-3 left-3 right-3 rounded-lg bg-white/97 p-3 shadow-lg ring-1 ring-neutral-200 md:right-auto md:max-w-sm">
      <div className="flex items-start gap-2">
        <span aria-hidden className="text-xl">{isQuake ? "⛰️" : "🔥"}</span>
        <div className="min-w-0 flex-1">
          <div className="truncate text-sm font-semibold text-neutral-800">{title}</div>
          <div className="mt-0.5 flex items-center gap-1.5 text-xs">
            <span className="inline-block h-2.5 w-2.5 rounded-full" style={{ background: SEVERITY_COLOR[sev] }} aria-hidden />
            <span className="font-medium text-neutral-700">{sevLabel[lang]}</span>
            <span className="text-neutral-400">· {relativeAgo(timeMs, lang)}</span>
          </div>
        </div>
        <button type="button" onClick={onClose} aria-label={t("close")} className="btn-ghost -mr-1 -mt-1 px-2 py-1 text-neutral-500">
          ✕
        </button>
      </div>

      <dl className="mt-2 grid grid-cols-2 gap-x-3 gap-y-1 text-xs">
        {isQuake ? (
          <>
            {selected.quake.magnitude != null && (
              <Field label={t("magnitude")} value={`M${selected.quake.magnitude.toFixed(1)}`} />
            )}
            {selected.quake.depthKm != null && (
              <Field label={t("depth")} value={`${selected.quake.depthKm.toFixed(0)} km`} />
            )}
          </>
        ) : (
          <>
            {selected.fire.frpMw != null && (
              <Field label={t("radiated_power")} value={`${selected.fire.frpMw.toFixed(0)} MW`} />
            )}
            {selected.fire.brightnessK != null && (
              <Field label={t("brightness")} value={`${selected.fire.brightnessK.toFixed(0)} K`} />
            )}
          </>
        )}
        <Field label={t("source_label")} value={isQuake ? selected.quake.source : selected.fire.source} />
        <Field label="" value={absTime} />
      </dl>

      {isQuake && selected.quake.url && (
        <a href={selected.quake.url} target="_blank" rel="noreferrer" className="mt-2 inline-block text-xs font-medium text-primary hover:underline">
          {t("view_usgs")} ↗
        </a>
      )}
    </div>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className="min-w-0">
      {label && <dt className="text-neutral-400">{label}</dt>}
      <dd className="truncate text-neutral-700">{value}</dd>
    </div>
  );
}

function Legend() {
  const { t } = useI18n();
  return (
    <div className="absolute bottom-3 left-3 rounded-md bg-white/95 px-3 py-2 text-xs shadow ring-1 ring-neutral-200">
      <div className="mb-1 font-semibold text-neutral-600">{t("legend")}</div>
      <div className="flex flex-col gap-1">
        <span><span aria-hidden>⛰️</span> {t("layer_quakes")} &nbsp; <span aria-hidden>🔥</span> {t("layer_fires")}</span>
        <span className="flex items-center gap-2">
          <LegendDot c={SEVERITY_COLOR.low} label={t("sev_low")} />
          <LegendDot c={SEVERITY_COLOR.moderate} label={t("sev_moderate")} />
          <LegendDot c={SEVERITY_COLOR.high} label={t("sev_high")} />
        </span>
      </div>
    </div>
  );
}

function LegendDot({ c, label }: { c: string; label: string }) {
  return (
    <span className="inline-flex items-center gap-1">
      <span className="inline-block h-2.5 w-2.5 rounded-full ring-2" style={{ background: "#fff", borderColor: c, boxShadow: `0 0 0 2px ${c}` }} aria-hidden />
      {label}
    </span>
  );
}
