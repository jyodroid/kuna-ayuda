import "./styles.css";

/**
 * Tiny hand-rolled i18n for a static ES/EN site. Every translatable chunk is written twice in the
 * HTML — `[data-lang="es"]` and `[data-lang="en"]` — and we simply show the active language and hide
 * the other (so ES renders with no JS and stays SEO-visible). The choice persists in localStorage.
 */
type Lang = "es" | "en";
const STORAGE_KEY = "kuna_lang";

function currentLang(): Lang {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved === "es" || saved === "en") return saved;
  // Default to Spanish for everyone (regardless of browser language); EN only if the user picks it.
  return "es";
}

function applyLang(lang: Lang): void {
  document.documentElement.lang = lang;

  document.querySelectorAll<HTMLElement>("[data-lang]").forEach((el) => {
    el.hidden = el.getAttribute("data-lang") !== lang;
  });

  // Document <title> is swapped from hidden [data-doctitle] holders (CSS keeps them unrendered).
  const title = document.querySelector<HTMLElement>(`[data-doctitle][data-lang="${lang}"]`);
  if (title?.textContent) document.title = title.textContent.trim();

  document.querySelectorAll<HTMLElement>("[data-set-lang]").forEach((btn) => {
    btn.setAttribute("aria-pressed", String(btn.getAttribute("data-set-lang") === lang));
  });
}

function init(): void {
  applyLang(currentLang());

  document.querySelectorAll<HTMLElement>("[data-set-lang]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const lang = btn.getAttribute("data-set-lang") as Lang;
      localStorage.setItem(STORAGE_KEY, lang);
      applyLang(lang);
    });
  });

  const year = document.querySelector("[data-year]");
  if (year) year.textContent = String(new Date().getFullYear());
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", init);
} else {
  init();
}
