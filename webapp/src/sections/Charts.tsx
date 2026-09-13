import { useMemo } from "react";
import { Bar, BarChart, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import type { Fire, Quake } from "../api";
import type { CountryCode } from "../content/countries";
import { useI18n } from "../i18n";
import { nearestCity } from "../map/firePlace";
import { fireWeight, SEVERITY_COLOR } from "../map/severity";

// Charts strip under the map: where fire intensity concentrates (bar by nearest city) + the quake
// magnitude distribution. Both summarize the full country feed, independent of the map layer toggles.

const FIRE_COLOR = "#EF6C00";
const MAX_GROUP_KM = 500; // fires farther than this from any city fall into "Other areas"
const TOP_N = 8;

export function Charts({ quakes, fires, country }: { quakes: Quake[]; fires: Fire[]; country: CountryCode }) {
  const { t, lang } = useI18n();

  const fireData = useMemo(() => {
    const byArea = new Map<string, number>();
    for (const f of fires) {
      const near = nearestCity(f.latitude, f.longitude, country);
      const name = f.place?.trim() || (near && near.km <= MAX_GROUP_KM ? near.city.name : t("other_areas"));
      byArea.set(name, (byArea.get(name) ?? 0) + fireWeight(f));
    }
    return [...byArea.entries()]
      .map(([name, mw]) => ({ name, mw: Math.round(mw) }))
      .sort((a, b) => b.mw - a.mw)
      .slice(0, TOP_N);
  }, [fires, country, t]);

  const quakeData = useMemo(() => {
    const bands = [
      { key: "light", label: t("band_light"), color: SEVERITY_COLOR.low, test: (m: number) => m < 3 },
      { key: "moderate", label: t("band_moderate"), color: SEVERITY_COLOR.moderate, test: (m: number) => m >= 3 && m < 4.5 },
      { key: "strong", label: t("band_strong"), color: "#EF6C00", test: (m: number) => m >= 4.5 && m < 6 },
      { key: "major", label: t("band_major"), color: SEVERITY_COLOR.high, test: (m: number) => m >= 6 },
    ];
    return bands.map((b) => ({
      name: b.label,
      count: quakes.filter((q) => q.magnitude != null && b.test(q.magnitude)).length,
      color: b.color,
    }));
  }, [quakes, t]);

  return (
    <div className="mt-4 grid gap-4 md:grid-cols-2">
      <ChartCard title={t("chart_fire_title")} subtitle={t("chart_fire_sub")} empty={fireData.length === 0}>
        <ResponsiveContainer width="100%" height={Math.max(140, fireData.length * 30)}>
          <BarChart data={fireData} layout="vertical" margin={{ left: 8, right: 16, top: 4, bottom: 4 }}>
            <XAxis type="number" tick={{ fontSize: 11 }} />
            <YAxis type="category" dataKey="name" width={120} tick={{ fontSize: 11 }} />
            <Tooltip formatter={(v: number) => [`${v} MW`, ""]} labelStyle={{ fontSize: 12 }} />
            <Bar dataKey="mw" fill={FIRE_COLOR} radius={[0, 4, 4, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </ChartCard>

      <ChartCard title={t("chart_quake_title")} subtitle={t("chart_quake_sub")} empty={quakes.length === 0}>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={quakeData} margin={{ left: 0, right: 8, top: 4, bottom: 4 }}>
            <XAxis dataKey="name" tick={{ fontSize: 10 }} interval={0} />
            <YAxis allowDecimals={false} width={28} tick={{ fontSize: 11 }} />
            <Tooltip labelStyle={{ fontSize: 12 }} formatter={(v: number) => [v, lang === "es" ? "sismos" : "quakes"]} />
            <Bar dataKey="count" radius={[4, 4, 0, 0]}>
              {quakeData.map((d) => (
                <Cell key={d.name} fill={d.color} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </ChartCard>
    </div>
  );
}

function ChartCard({ title, subtitle, empty, children }: { title: string; subtitle: string; empty: boolean; children: React.ReactNode }) {
  const { t } = useI18n();
  return (
    <section className="rounded-lg border border-neutral-200 bg-white p-3">
      <h2 className="text-sm font-semibold text-neutral-800">{title}</h2>
      <p className="mb-2 text-xs text-neutral-500">{subtitle}</p>
      {empty ? <p className="py-8 text-center text-sm text-neutral-400">{t("chart_empty")}</p> : children}
    </section>
  );
}
