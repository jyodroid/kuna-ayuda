import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { kuna, type Shelter } from "../api";
import { useCountry } from "../state";
import { useI18n, type MsgKey } from "../i18n";
import { useGeolocation } from "../map/useGeolocation";
import { nearestCity } from "../map/firePlace";
import { distanceKm } from "../map/severity";

// Help points as a LIST (not a map layer) — the map stays hazards-only to avoid mixing help points
// with earthquakes/wildfires. Each card carries directions + contact. Near-me is a revertible toggle
// (mirrors the map), and there's a city filter (city derived from each point's nearest known city).

const TYPE_ICON: Record<string, string> = { ACOPIO: "📦", ALBERGUE: "🏠", SALUD: "🏥", AGUA: "💧", OTRO: "📍" };
const TYPE_KEY: Record<string, MsgKey> = {
  ACOPIO: "stype_acopio",
  ALBERGUE: "stype_albergue",
  SALUD: "stype_salud",
  AGUA: "stype_agua",
  OTRO: "stype_otro",
};

function directionsUrl(lat: number, lon: number): string {
  return `https://www.google.com/maps/dir/?api=1&destination=${lat},${lon}`;
}

export function SheltersPage() {
  const { t } = useI18n();
  const { country } = useCountry();
  const geo = useGeolocation();

  const [shelters, setShelters] = useState<Shelter[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [typeFilter, setTypeFilter] = useState<string | null>(null);
  const [cityFilter, setCityFilter] = useState<string>(""); // "" = all cities
  const [nearMode, setNearMode] = useState(false);
  const autoNearRef = useRef(false);

  const nearActive = nearMode && geo.status === "granted" && geo.loc != null;

  const load = useCallback(async () => {
    setLoading(true);
    setError(false);
    try {
      setShelters(await kuna.shelters(country));
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [country]);

  useEffect(() => {
    setTypeFilter(null);
    setCityFilter("");
    setNearMode(false);
    load();
  }, [load]);

  // Auto-enable near-me once, the first time location is granted.
  useEffect(() => {
    if (geo.status === "granted" && geo.loc && !autoNearRef.current) {
      autoNearRef.current = true;
      setNearMode(true);
    }
  }, [geo.status, geo.loc]);

  // Assign each shelter its nearest known city (for the city filter + grouping).
  const withCity = useMemo(
    () => shelters.map((s) => ({ s, city: nearestCity(s.latitude, s.longitude, country)?.city.name ?? "—" })),
    [shelters, country],
  );

  const cities = useMemo(() => [...new Set(withCity.map((x) => x.city))].sort((a, b) => a.localeCompare(b)), [withCity]);
  const types = useMemo(() => [...new Set(shelters.map((s) => s.type.toUpperCase()))], [shelters]);

  const list = useMemo(() => {
    const u = nearActive ? geo.loc : null;
    let l = withCity;
    if (typeFilter) l = l.filter((x) => x.s.type.toUpperCase() === typeFilter);
    if (cityFilter) l = l.filter((x) => x.city === cityFilter);
    const rows = l.map((x) => ({
      ...x,
      km: u ? distanceKm(u.lat, u.lon, x.s.latitude, x.s.longitude) : null,
    }));
    rows.sort((a, b) => (u ? (a.km ?? 0) - (b.km ?? 0) : a.s.name.localeCompare(b.s.name)));
    return rows;
  }, [withCity, typeFilter, cityFilter, nearActive, geo.loc]);

  return (
    <div className="mx-auto max-w-3xl px-4 py-4">
      <div className="mb-3 flex flex-wrap items-center gap-2">
        {/* Near-me ↔ list toggle (revertible, mirrors the map). */}
        <div className="flex overflow-hidden rounded-full text-sm font-medium ring-1 ring-neutral-300">
          <button
            type="button"
            aria-pressed={nearActive}
            onClick={() => (geo.status === "granted" ? setNearMode(true) : geo.request())}
            className={`px-3 py-1.5 ${nearActive ? "bg-primary text-white" : "bg-white hover:bg-neutral-50"}`}
          >
            📍 {t("near_me")}
          </button>
          <button
            type="button"
            aria-pressed={!nearActive}
            onClick={() => setNearMode(false)}
            className={`px-3 py-1.5 ${!nearActive ? "bg-primary text-white" : "bg-white hover:bg-neutral-50"}`}
          >
            {t("filter_all")}
          </button>
        </div>

        {/* City filter. */}
        <label className="sr-only" htmlFor="city">{t("city")}</label>
        <select id="city" className="input py-1" value={cityFilter} onChange={(e) => setCityFilter(e.target.value)}>
          <option value="">{t("all_cities")}</option>
          {cities.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </div>

      {/* Type filter chips. */}
      <div className="mb-3 flex flex-wrap items-center gap-2">
        <FilterChip label={t("filter_all")} on={typeFilter === null} onClick={() => setTypeFilter(null)} />
        {types.map((ty) => (
          <FilterChip key={ty} label={t(TYPE_KEY[ty] ?? "stype_otro")} on={typeFilter === ty} onClick={() => setTypeFilter(ty)} />
        ))}
      </div>

      {geo.status === "denied" && <p className="mb-2 rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-800">{t("near_me_denied")}</p>}

      {loading ? (
        <p className="p-4 text-sm text-neutral-500" aria-live="polite">{t("loading")}</p>
      ) : error ? (
        <div className="p-4 text-sm">
          <p className="mb-2 text-neutral-600">{t("load_error")}</p>
          <button className="btn-primary" onClick={load}>{t("retry")}</button>
        </div>
      ) : list.length === 0 ? (
        <p className="p-4 text-sm text-neutral-500">{t("empty")}</p>
      ) : (
        <ul className="space-y-3">
          {list.map(({ s, km }) => (
            <ShelterCard key={s.id} s={s} km={km} />
          ))}
        </ul>
      )}

      <p className="mt-4 text-center text-xs text-neutral-400">{t("shelters_note")}</p>
    </div>
  );
}

function FilterChip({ label, on, onClick }: { label: string; on: boolean; onClick: () => void }) {
  return (
    <button type="button" onClick={onClick} aria-pressed={on} className={`chip ${on ? "chip-on" : ""}`}>
      {label}
    </button>
  );
}

function ShelterCard({ s, km }: { s: Shelter; km: number | null }) {
  const { t, lang } = useI18n();
  const typeLabel = t(TYPE_KEY[s.type.toUpperCase()] ?? "stype_otro");
  return (
    <li className="rounded-lg border border-neutral-200 bg-white p-4">
      <div className="flex items-start gap-2">
        <span aria-hidden className="text-xl">{TYPE_ICON[s.type.toUpperCase()] ?? "📍"}</span>
        <div className="min-w-0 flex-1">
          <h3 className="font-semibold text-neutral-800">{s.name}</h3>
          <p className="text-xs text-neutral-500">
            {typeLabel}
            {s.verified && <span className="ml-2 rounded bg-primary-muted px-1.5 py-0.5 text-[11px] font-medium text-primary">✓ {t("verified")}</span>}
            {km != null && <span className="ml-2">· {lang === "es" ? `a ${km.toFixed(0)} km` : `${km.toFixed(0)} km away`}</span>}
          </p>
        </div>
      </div>

      <dl className="mt-2 space-y-1 text-sm text-neutral-600">
        <Line label="" value={s.address} />
        {s.accepts && <Line label={t("accepts")} value={s.accepts} />}
        {s.hours && <Line label={t("hours")} value={s.hours} />}
      </dl>

      <div className="mt-3 flex flex-wrap gap-2">
        <a href={directionsUrl(s.latitude, s.longitude)} target="_blank" rel="noreferrer" className="btn-primary">
          {t("how_to_get")}
        </a>
        {s.contactPhone && (
          <a href={`tel:${s.contactPhone.replace(/[^\d+*#]/g, "")}`} className="rounded-md border border-neutral-300 px-4 py-2 text-sm font-medium text-neutral-700 hover:bg-neutral-50">
            {t("call")} {s.contactPhone}
          </a>
        )}
      </div>
    </li>
  );
}

function Line({ label, value }: { label: string; value: string }) {
  return (
    <div>
      {label && <span className="font-medium text-neutral-500">{label}: </span>}
      {value}
    </div>
  );
}
