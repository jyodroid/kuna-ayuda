import { useCallback, useEffect, useMemo, useState } from "react";
import { kuna, type BoardPost } from "../api";
import { useCountry } from "../state";
import { useI18n, type MsgKey } from "../i18n";

// Mutual-aid board (read-only): community requests/offers of resources. ACTIVE only, last 30 days.
// Filter by kind (Solicita/Ofrece) and resource type. Posting from the web is deferred.

const RTYPE_KEY: Record<string, MsgKey> = {
  WATER: "rtype_water",
  FOOD: "rtype_food",
  MEDICINE: "rtype_medicine",
  SHELTER: "rtype_shelter",
  HYGIENE: "rtype_hygiene",
  OTHER: "rtype_other",
};

function telHref(phone: string): string {
  return `tel:${phone.replace(/[^\d+*#]/g, "")}`;
}

export function BoardPage() {
  const { t } = useI18n();
  const { country } = useCountry();

  const [posts, setPosts] = useState<BoardPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);
  const [kind, setKind] = useState<string | null>(null); // REQUEST | OFFER | null
  const [rtype, setRtype] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(false);
    try {
      setPosts(await kuna.board(country, kind ?? undefined, rtype ?? undefined));
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [country, kind, rtype]);

  useEffect(() => {
    load();
  }, [load]);

  const rtypes = useMemo(() => [...new Set(posts.map((p) => p.resourceType.toUpperCase()))], [posts]);

  return (
    <div className="mx-auto max-w-3xl px-4 py-4">
      <div className="mb-3 flex flex-wrap items-center gap-2">
        <Chip label={t("filter_all")} on={kind === null} onClick={() => setKind(null)} />
        <Chip label={t("kind_request")} on={kind === "REQUEST"} onClick={() => setKind("REQUEST")} />
        <Chip label={t("kind_offer")} on={kind === "OFFER"} onClick={() => setKind("OFFER")} />
        <span className="mx-1 h-5 w-px bg-neutral-200" />
        <Chip label={t("filter_all")} on={rtype === null} onClick={() => setRtype(null)} />
        {rtypes.map((rt) => (
          <Chip key={rt} label={t(RTYPE_KEY[rt] ?? "rtype_other")} on={rtype === rt} onClick={() => setRtype(rt)} />
        ))}
      </div>

      {loading ? (
        <p className="p-4 text-sm text-neutral-500" aria-live="polite">{t("loading")}</p>
      ) : error ? (
        <div className="p-4 text-sm">
          <p className="mb-2 text-neutral-600">{t("load_error")}</p>
          <button className="btn-primary" onClick={load}>{t("retry")}</button>
        </div>
      ) : posts.length === 0 ? (
        <p className="p-4 text-sm text-neutral-500">{t("board_empty")}</p>
      ) : (
        <ul className="space-y-3">
          {posts.map((p) => (
            <PostCard key={p.id} p={p} />
          ))}
        </ul>
      )}

      <p className="mt-4 text-center text-xs text-neutral-400">{t("window_30d")}</p>
    </div>
  );
}

function Chip({ label, on, onClick }: { label: string; on: boolean; onClick: () => void }) {
  return (
    <button type="button" onClick={onClick} aria-pressed={on} className={`chip ${on ? "chip-on" : ""}`}>
      {label}
    </button>
  );
}

function PostCard({ p }: { p: BoardPost }) {
  const { t } = useI18n();
  const isRequest = p.kind.toUpperCase() === "REQUEST";
  const rtypeLabel = t(RTYPE_KEY[p.resourceType.toUpperCase()] ?? "rtype_other");
  return (
    <li className="rounded-lg border border-neutral-200 bg-white p-4">
      <div className="mb-1 flex flex-wrap items-center gap-2">
        <span className={`rounded px-2 py-0.5 text-xs font-medium ${isRequest ? "bg-amber-100 text-amber-800" : "bg-primary-muted text-primary"}`}>
          {isRequest ? t("kind_request") : t("kind_offer")}
        </span>
        <span className="rounded bg-neutral-100 px-2 py-0.5 text-xs text-neutral-600">{rtypeLabel}</span>
        {p.region && <span className="text-xs text-neutral-500">📍 {p.region}</span>}
      </div>

      <p className="whitespace-pre-line text-sm text-neutral-700">{p.description}</p>

      {p.collectionPoints && p.collectionPoints.length > 0 && (
        <div className="mt-2 rounded-md bg-neutral-50 p-2 text-xs text-neutral-600">
          <div className="mb-1 font-semibold">{t("collection_points")}</div>
          <ul className="space-y-0.5">
            {p.collectionPoints.map((cp, i) => (
              <li key={i}>• {[cp.name, cp.address, cp.hours].filter(Boolean).join(" · ")}</li>
            ))}
          </ul>
        </div>
      )}

      {(p.contactName || p.contactPhone || p.contactEmail) && (
        <div className="mt-3 flex flex-wrap items-center gap-2 text-sm">
          <span className="text-xs font-medium text-neutral-500">{t("contact")}:</span>
          {p.contactName && <span className="text-neutral-700">{p.contactName}</span>}
          {p.contactPhone && (
            <a href={telHref(p.contactPhone)} className="font-medium text-primary hover:underline">
              {p.contactPhone}
            </a>
          )}
          {p.contactEmail && (
            <a href={`mailto:${p.contactEmail}`} className="font-medium text-primary hover:underline">
              {p.contactEmail}
            </a>
          )}
        </div>
      )}
    </li>
  );
}
