import { useCallback, useEffect, useState } from "react";
import { kuna, type SafeCheckIn, type SearchReport } from "../api";
import { useCountry } from "../state";
import { useI18n } from "../i18n";
import { Photo } from "../components/Photo";
import { relativeAgo } from "../map/severity";

// Lost & Found (pets + people) + the public "I'm safe" reassurance list. Read-only; last 30 days.

type Tab = "reports" | "safe";

export function SearchPage() {
  const { t } = useI18n();
  const [tab, setTab] = useState<Tab>("reports");

  return (
    <div className="mx-auto max-w-3xl px-4 py-4">
      <div className="mb-4 flex overflow-hidden rounded-lg border border-neutral-200 text-sm font-medium">
        <TabBtn on={tab === "reports"} onClick={() => setTab("reports")} label={t("tab_reports")} />
        <TabBtn on={tab === "safe"} onClick={() => setTab("safe")} label={t("tab_safe")} />
      </div>
      {tab === "reports" ? <Reports /> : <Safe />}
    </div>
  );
}

// Placeholder shown when a report has no photo, or its photo is missing/broken. Uses Font Awesome
// (a paw for pets, a person for people) instead of the browser's broken-image icon.
function PhotoFallback({ pet }: { pet: boolean }) {
  return (
    <div className="flex h-20 w-20 shrink-0 items-center justify-center rounded-md bg-neutral-100 text-neutral-400" aria-hidden>
      <i className={`fa-solid ${pet ? "fa-paw" : "fa-user"} text-2xl`} />
    </div>
  );
}

function TabBtn({ on, onClick, label }: { on: boolean; onClick: () => void; label: string }) {
  return (
    <button type="button" onClick={onClick} aria-pressed={on} className={`flex-1 px-3 py-2 ${on ? "bg-primary text-white" : "bg-white text-neutral-700 hover:bg-neutral-50"}`}>
      {label}
    </button>
  );
}

function Chip({ label, on, onClick }: { label: string; on: boolean; onClick: () => void }) {
  return (
    <button type="button" onClick={onClick} aria-pressed={on} className={`chip ${on ? "chip-on" : ""}`}>
      {label}
    </button>
  );
}

function Reports() {
  const { t, lang } = useI18n();
  const { country } = useCountry();
  const [reports, setReports] = useState<SearchReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [subject, setSubject] = useState<string | null>(null);
  const [state, setState] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(false);
    try {
      setReports(await kuna.search(country, subject ?? undefined, state ?? undefined));
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [country, subject, state]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <>
      <div className="mb-3 flex flex-wrap items-center gap-2">
        <Chip label={t("filter_all")} on={subject === null} onClick={() => setSubject(null)} />
        <Chip label={t("subject_pet")} on={subject === "PET"} onClick={() => setSubject("PET")} />
        <Chip label={t("subject_person")} on={subject === "PERSON"} onClick={() => setSubject("PERSON")} />
        <span className="mx-1 h-5 w-px bg-neutral-200" />
        <Chip label={t("filter_all")} on={state === null} onClick={() => setState(null)} />
        <Chip label={t("state_lost")} on={state === "LOST"} onClick={() => setState("LOST")} />
        <Chip label={t("state_found")} on={state === "FOUND"} onClick={() => setState("FOUND")} />
      </div>

      {loading ? (
        <p className="p-4 text-sm text-neutral-500" aria-live="polite">{t("loading")}</p>
      ) : error ? (
        <div className="p-4 text-sm">
          <p className="mb-2 text-neutral-600">{t("load_error")}</p>
          <button className="btn-primary" onClick={load}>{t("retry")}</button>
        </div>
      ) : reports.length === 0 ? (
        <p className="p-4 text-sm text-neutral-500">{t("empty")}</p>
      ) : (
        <ul className="space-y-3">
          {reports.map((r) => (
            <li key={r.id} className="flex gap-3 rounded-lg border border-neutral-200 bg-white p-4">
              {r.photoId != null ? (
                <Photo
                  id={r.photoId}
                  alt={r.title}
                  className="h-20 w-20 shrink-0 rounded-md object-cover"
                  fallback={<PhotoFallback pet={r.subject.toUpperCase() === "PET"} />}
                />
              ) : (
                <PhotoFallback pet={r.subject.toUpperCase() === "PET"} />
              )}
              <div className="min-w-0 flex-1">
                <div className="mb-1 flex flex-wrap items-center gap-2">
                  <span className="rounded bg-neutral-100 px-2 py-0.5 text-xs text-neutral-600">
                    {t(r.subject.toUpperCase() === "PET" ? "subject_pet" : "subject_person")}
                  </span>
                  <span className={`rounded px-2 py-0.5 text-xs font-medium ${r.state.toUpperCase() === "LOST" ? "bg-amber-100 text-amber-800" : "bg-primary-muted text-primary"}`}>
                    {t(r.state.toUpperCase() === "LOST" ? "state_lost" : "state_found")}
                  </span>
                </div>
                <h3 className="font-semibold text-neutral-800">{r.title}</h3>
                <p className="text-sm text-neutral-600">{r.description}</p>
                {r.lastSeen && <p className="mt-1 text-xs text-neutral-500">{t("last_seen")}: {r.lastSeen}</p>}
                <div className="mt-2 text-sm">
                  {r.contactName && <span className="text-neutral-700">{r.contactName} · </span>}
                  <a href={`tel:${r.contactPhone.replace(/[^\d+*#]/g, "")}`} className="font-medium text-primary hover:underline">
                    {r.contactPhone}
                  </a>
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
      <p className="mt-4 text-center text-xs text-neutral-400">{t("window_30d")}</p>
    </>
  );
}

function Safe() {
  const { t, lang } = useI18n();
  const { country } = useCountry();
  const [items, setItems] = useState<SafeCheckIn[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(false);
    try {
      setItems(await kuna.safe(country));
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [country]);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <>
      <p className="mb-3 rounded-md bg-primary-muted px-3 py-2 text-sm text-primary">{t("safe_lead")}</p>
      {loading ? (
        <p className="p-4 text-sm text-neutral-500" aria-live="polite">{t("loading")}</p>
      ) : error ? (
        <div className="p-4 text-sm">
          <p className="mb-2 text-neutral-600">{t("load_error")}</p>
          <button className="btn-primary" onClick={load}>{t("retry")}</button>
        </div>
      ) : items.length === 0 ? (
        <p className="p-4 text-sm text-neutral-500">{t("safe_empty")}</p>
      ) : (
        <ul className="space-y-2">
          {items.map((s) => (
            <li key={s.id} className="flex items-center gap-3 rounded-lg border border-neutral-200 bg-white px-4 py-3">
              <span aria-hidden className="text-lg">✅</span>
              <div className="min-w-0 flex-1">
                <span className="font-medium text-neutral-800">{s.name}</span>
                {s.region && <span className="text-sm text-neutral-500"> · {s.region}</span>}
              </div>
              <span className="shrink-0 text-xs text-neutral-400">{relativeAgo(s.createdAtEpochMs, lang)}</span>
            </li>
          ))}
        </ul>
      )}
      <p className="mt-4 text-center text-xs text-neutral-400">{t("window_30d")}</p>
    </>
  );
}
