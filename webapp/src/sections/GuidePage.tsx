import { useCallback, useEffect, useState } from "react";
import { kuna, type Guide, type GuideTip } from "../api";
import { useCountry } from "../state";
import { useI18n } from "../i18n";
import { tipSpeech, useSpeech } from "../tts";

// Emergency guide + safety tips, from the central /api/guide endpoint (single source of truth shared
// across platforms). Country-specific numbers + ES/EN tips.

const CAT_LABEL: Record<string, { es: string; en: string; icon: string }> = {
  GENERAL: { es: "Emergencias", en: "Emergency", icon: "fa-triangle-exclamation" },
  POLICE: { es: "Policía", en: "Police", icon: "fa-shield-halved" },
  FIRE: { es: "Bomberos", en: "Fire brigade", icon: "fa-fire-extinguisher" },
  MEDICAL: { es: "Emergencias médicas", en: "Medical", icon: "fa-truck-medical" },
  RED_CROSS: { es: "Cruz Roja", en: "Red Cross", icon: "fa-plus" },
  CIVIL_DEFENSE: { es: "Defensa Civil", en: "Civil Defense", icon: "fa-helmet-safety" },
  SAR: { es: "Búsqueda y rescate", en: "Search & rescue", icon: "fa-life-ring" },
  DISASTER: { es: "Gestión de desastres", en: "Disaster agency", icon: "fa-house-crack" },
  MENTAL_HEALTH: { es: "Salud mental", en: "Mental health", icon: "fa-heart" },
};

const PHASE_ICON: Record<string, string> = {
  before: "fa-list-check",
  during: "fa-person-shelter",
  after: "fa-hand-holding-medical",
  mental: "fa-heart-pulse",
  animals: "fa-paw",
};

function telHref(phone: string): string {
  return `tel:${phone.replace(/[^\d+*#]/g, "")}`;
}

export function GuidePage() {
  const { t, lang } = useI18n();
  const { country } = useCountry();
  const speech = useSpeech();
  const [guide, setGuide] = useState<Guide | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(false);
    try {
      setGuide(await kuna.guide(country, lang));
    } catch {
      setError(true);
    } finally {
      setLoading(false);
    }
  }, [country, lang]);

  useEffect(() => {
    load();
  }, [load]);

  if (loading) return <p className="mx-auto max-w-3xl p-6 text-sm text-neutral-500" aria-live="polite">{t("loading")}</p>;
  if (error || !guide)
    return (
      <div className="mx-auto max-w-3xl p-6 text-sm">
        <p className="mb-2 text-neutral-600">{t("load_error")}</p>
        <button className="btn-primary" onClick={load}>{t("retry")}</button>
      </div>
    );

  return (
    <div className="mx-auto max-w-3xl px-4 py-4">
      {/* Emergency numbers */}
      <h2 className="mb-2 text-lg font-bold text-neutral-800">{t("guide_emergency")}</h2>
      <a
        href={telHref(guide.emergency.general)}
        className="mb-3 flex items-center justify-between rounded-lg bg-danger px-4 py-3 text-white"
      >
        <span className="font-semibold">
          <i className="fa-solid fa-phone mr-2" aria-hidden /> {t("guide_general")}
        </span>
        <span className="text-2xl font-bold">{guide.emergency.general}</span>
      </a>

      <ul className="mb-6 grid gap-2 sm:grid-cols-2">
        {guide.emergency.contacts.map((c, i) => {
          const meta = CAT_LABEL[c.category] ?? CAT_LABEL.GENERAL;
          return (
            <li key={i}>
              <a href={telHref(c.phone)} className="flex items-center gap-3 rounded-lg border border-neutral-200 bg-white p-3 hover:bg-neutral-50">
                <i className={`fa-solid ${meta.icon} w-5 text-center text-primary`} aria-hidden />
                <span className="min-w-0 flex-1">
                  <span className="block text-xs text-neutral-500">{lang === "es" ? meta.es : meta.en}</span>
                  <span className="block truncate text-sm font-medium text-neutral-800">{c.name}</span>
                </span>
                <span className="font-bold text-primary">{c.phone}</span>
              </a>
            </li>
          );
        })}
      </ul>

      {/* Safety tips */}
      <h2 className="mb-2 text-lg font-bold text-neutral-800">{t("guide_tips")}</h2>
      {guide.tips.map((phase) => (
        <section key={phase.key} className="mb-5">
          <h3 className="mb-2 flex items-center gap-2 font-semibold text-neutral-700">
            <i className={`fa-solid ${PHASE_ICON[phase.key] ?? "fa-circle-info"} text-primary`} aria-hidden />
            {phase.label}
          </h3>
          <ul className="space-y-2">
            {phase.tips.map((tip) => (
              <TipCard key={tip.id} tip={tip} speech={speech} />
            ))}
          </ul>
        </section>
      ))}
    </div>
  );
}

function TipCard({ tip, speech }: { tip: GuideTip; speech: ReturnType<typeof useSpeech> }) {
  const { t, lang } = useI18n();
  const [open, setOpen] = useState(false);
  const speaking = speech.speakingId === tip.id;
  const base = import.meta.env.BASE_URL; // "/app/"

  return (
    <li className="rounded-lg border border-neutral-200 bg-white p-3">
      <p className="text-sm font-semibold text-neutral-800">{tip.title}</p>
      <p className="mt-0.5 whitespace-pre-line text-sm text-neutral-600">{tip.body}</p>

      <div className="mt-2 flex flex-wrap gap-2">
        {speech.available && (
          <button
            type="button"
            onClick={() => (speaking ? speech.stop() : speech.speak(tip.id, tipSpeech(tip.title, tip.body), lang))}
            className="inline-flex items-center gap-1.5 rounded-md border border-neutral-300 px-2.5 py-1 text-xs font-medium text-neutral-700 hover:bg-neutral-50"
          >
            <i className={`fa-solid ${speaking ? "fa-stop" : "fa-volume-high"}`} aria-hidden />
            {speaking ? t("stop") : t("listen")}
          </button>
        )}
        {tip.steps.length > 0 && (
          <button
            type="button"
            onClick={() => setOpen((v) => !v)}
            aria-expanded={open}
            className="inline-flex items-center gap-1.5 rounded-md border border-neutral-300 px-2.5 py-1 text-xs font-medium text-neutral-700 hover:bg-neutral-50"
          >
            <i className="fa-solid fa-images" aria-hidden />
            {open ? t("hide_steps") : t("show_steps")}
          </button>
        )}
      </div>

      {open && tip.steps.length > 0 && (
        <div className="mt-3">
          <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-neutral-400">{t("steps_heading")}</p>
          <ol className="grid grid-cols-3 gap-2">
            {tip.steps.map((step, i) => (
              <li key={step.img} className="text-center">
                <div className="relative overflow-hidden rounded-md bg-neutral-50">
                  <span className="absolute left-1 top-1 flex h-5 w-5 items-center justify-center rounded-full bg-primary text-xs font-bold text-white">{i + 1}</span>
                  <img src={`${base}story/${step.img}`} alt={step.caption} loading="lazy" className="aspect-square w-full object-contain" />
                </div>
                <p className="mt-1 text-xs text-neutral-600">{step.caption}</p>
              </li>
            ))}
          </ol>
        </div>
      )}
    </li>
  );
}
