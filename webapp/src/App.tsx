import { NavLink, Route, Routes } from "react-router-dom";
import { useI18n, type Lang, type MsgKey } from "./i18n";
import { useCountry } from "./state";
import { COUNTRIES } from "./content/countries";
import { PanelPage } from "./sections/PanelPage";
import { SheltersPage } from "./sections/SheltersPage";
import { BoardPage } from "./sections/BoardPage";
import { SearchPage } from "./sections/SearchPage";
import { GuidePage } from "./sections/GuidePage";

// App shell: a top bar (brand · country switcher · ES/EN · section nav) over the routed sections.
// W1 ships the shell + placeholder sections; real data views land in W2–W6.

export function App() {
  return (
    <div className="flex min-h-full flex-col">
      <TopBar />
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<PanelPage />} />
          <Route path="/shelters" element={<SheltersPage />} />
          <Route path="/aid" element={<BoardPage />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="/guide" element={<GuidePage />} />
          <Route path="*" element={<Placeholder titleKey="nav_map" />} />
        </Routes>
      </main>
      <Footer />
    </div>
  );
}

function TopBar() {
  const { t, lang, setLang } = useI18n();
  const { country, setCountry } = useCountry();

  const navItem = ({ isActive }: { isActive: boolean }) =>
    `px-3 py-2 rounded-md text-sm font-medium ${
      isActive ? "bg-primary-muted text-primary" : "text-neutral-700 hover:bg-neutral-100"
    }`;

  return (
    <header className="sticky top-0 z-10 border-b border-neutral-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-x-4 gap-y-2 px-4 py-2">
        {/* Brand → marketing landing (which links back here). */}
        <a href="/" className="flex items-center gap-2" title="Kuna Ayuda">
          <img src={`${import.meta.env.BASE_URL}logo.svg`} alt="" className="h-7 w-7" />
          <span className="text-lg font-bold text-primary">{t("app_name")}</span>
        </a>

        <nav className="order-3 flex w-full gap-1 sm:order-none sm:w-auto">
          <NavLink to="/" className={navItem} end>
            {t("nav_map")}
          </NavLink>
          <NavLink to="/shelters" className={navItem}>
            {t("nav_shelters")}
          </NavLink>
          <NavLink to="/aid" className={navItem}>
            {t("nav_aid")}
          </NavLink>
          <NavLink to="/search" className={navItem}>
            {t("nav_search")}
          </NavLink>
          <NavLink to="/guide" className={navItem}>
            {t("nav_guide")}
          </NavLink>
        </nav>

        <div className="ml-auto flex items-center gap-2">
          <label className="sr-only" htmlFor="country">
            {lang === "es" ? "País" : "Country"}
          </label>
          <select
            id="country"
            className="input py-1"
            value={country}
            onChange={(e) => setCountry(e.target.value as typeof country)}
          >
            {COUNTRIES.map((c) => (
              <option key={c.code} value={c.code}>
                {c.flag} {lang === "es" ? c.nameEs : c.nameEn}
              </option>
            ))}
          </select>

          <div className="flex overflow-hidden rounded-md border border-neutral-300" role="group" aria-label="Language">
            {(["es", "en"] as Lang[]).map((l) => (
              <button
                key={l}
                type="button"
                onClick={() => setLang(l)}
                aria-pressed={lang === l}
                className={`px-2 py-1 text-sm font-medium ${
                  lang === l ? "bg-primary text-white" : "bg-white text-neutral-600 hover:bg-neutral-100"
                }`}
              >
                {l.toUpperCase()}
              </button>
            ))}
          </div>
        </div>
      </div>
    </header>
  );
}

function Placeholder({ titleKey }: { titleKey: MsgKey }) {
  const { t } = useI18n();
  return (
    <div className="mx-auto max-w-6xl px-4 py-16 text-center">
      <h1 className="text-2xl font-bold text-neutral-800">{t(titleKey)}</h1>
      <p className="mx-auto mt-2 max-w-md text-neutral-500">{t("soon_body")}</p>
    </div>
  );
}

function Footer() {
  const { t } = useI18n();
  return (
    <footer className="border-t border-neutral-200 bg-white px-4 py-4 text-center text-xs text-neutral-500">
      <p>{t("disclaimer")}</p>
      <p className="mt-1">
        <a href="/console/" className="text-neutral-400 hover:text-primary hover:underline">
          {t("moderators")}
        </a>
      </p>
    </footer>
  );
}
