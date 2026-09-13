import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";

// Tiny hand-rolled ES/EN i18n (no library). Default is Spanish; the choice persists in localStorage
// under the same "kuna_lang" key the marketing landing uses, so the toggle is consistent across both.

export type Lang = "es" | "en";
const STORAGE_KEY = "kuna_lang";

function initialLang(): Lang {
  const saved = localStorage.getItem(STORAGE_KEY);
  return saved === "en" ? "en" : "es";
}

// Every string as an { es, en } pair. Keep keys grouped by section for readability.
const DICT = {
  app_name: { es: "Kuna Ayuda", en: "Kuna Ayuda" },
  nav_map: { es: "Mapa", en: "Map" },
  nav_shelters: { es: "Puntos de ayuda", en: "Help points" },
  nav_aid: { es: "Red de ayuda", en: "Aid network" },
  nav_search: { es: "Búsqueda y reencuentro", en: "Search & reunite" },
  nav_guide: { es: "Guía", en: "Guide" },

  filter_all: { es: "Todos", en: "All" },
  list_view: { es: "Lista", en: "List" },
  city: { es: "Ciudad", en: "City" },
  all_cities: { es: "Todas las ciudades", en: "All cities" },
  // Data-window notes so users know how fresh / how far back each feed goes.
  panel_note: {
    es: "Sismos de los últimos ~30 días · incendios de las últimas ~48 h. Fuentes: USGS/SGC, NASA FIRMS/GDACS.",
    en: "Earthquakes from the last ~30 days · wildfires from the last ~48 h. Sources: USGS/SGC, NASA FIRMS/GDACS.",
  },
  shelters_note: {
    es: "Puntos oficiales verificados por el equipo de moderación.",
    en: "Official points verified by the moderation team.",
  },
  window_30d: {
    es: "Se muestran publicaciones de los últimos 30 días.",
    en: "Showing posts from the last 30 days.",
  },

  layer_quakes: { es: "Sismos", en: "Earthquakes" },
  layer_fires: { es: "Incendios", en: "Wildfires" },
  layer_shelters: { es: "Puntos de ayuda", en: "Help points" },

  near_me: { es: "Cerca de mí", en: "Near me" },
  country_view: { es: "País", en: "Country" },
  near_me_denied: {
    es: "No pudimos usar tu ubicación. Mostrando el país.",
    en: "Couldn't use your location. Showing the country.",
  },
  loading: { es: "Cargando…", en: "Loading…" },
  load_error: { es: "No se pudo cargar. Reintentar.", en: "Couldn't load. Retry." },
  retry: { es: "Reintentar", en: "Retry" },
  empty: { es: "Sin resultados por ahora.", en: "Nothing to show yet." },

  legend: { es: "Leyenda", en: "Legend" },
  close: { es: "Cerrar", en: "Close" },
  how_to_get: { es: "Cómo llegar", en: "Get directions" },
  accepts: { es: "Recibe", en: "Accepts" },
  hours: { es: "Horario", en: "Hours" },
  phone: { es: "Teléfono", en: "Phone" },
  call: { es: "Llamar", en: "Call" },
  verified: { es: "Verificado", en: "Verified" },
  stype_acopio: { es: "Centro de acopio", en: "Collection point" },
  stype_albergue: { es: "Albergue", en: "Shelter" },
  stype_salud: { es: "Salud", en: "Medical" },
  stype_agua: { es: "Agua", en: "Water" },
  stype_otro: { es: "Punto de ayuda", en: "Help point" },
  source_label: { es: "Fuente", en: "Source" },
  view_usgs: { es: "Ver detalle (USGS)", en: "View detail (USGS)" },
  radiated_power: { es: "Potencia radiada", en: "Radiated power" },
  brightness: { es: "Temperatura de brillo", en: "Brightness temp." },
  sev_low: { es: "Baja", en: "Low" },
  sev_moderate: { es: "Moderada", en: "Moderate" },
  sev_high: { es: "Alta", en: "High" },
  magnitude: { es: "Magnitud", en: "Magnitude" },
  depth: { es: "Profundidad", en: "Depth" },
  km_deep: { es: "km de profundidad", en: "km deep" },
  results_count: { es: "resultados", en: "results" },
  no_quakes: { es: "Sin sismos recientes en esta zona.", en: "No recent earthquakes here." },
  no_fires: { es: "Sin incendios activos en esta zona.", en: "No active wildfires here." },
  distance_away: { es: "a", en: "" }, // "a 12 km" / "12 km away"
  away: { es: "", en: "away" },

  // Aid board
  kind_request: { es: "Solicita", en: "Requests" },
  kind_offer: { es: "Ofrece", en: "Offers" },
  rtype_water: { es: "Agua", en: "Water" },
  rtype_food: { es: "Comida", en: "Food" },
  rtype_medicine: { es: "Medicina", en: "Medicine" },
  rtype_shelter: { es: "Refugio", en: "Shelter" },
  rtype_hygiene: { es: "Higiene", en: "Hygiene" },
  rtype_other: { es: "Otro", en: "Other" },
  region_label: { es: "Región", en: "Region" },
  contact: { es: "Contacto", en: "Contact" },
  collection_points: { es: "Puntos de recepción", en: "Drop-off points" },
  board_empty: { es: "No hay publicaciones activas.", en: "No active posts." },
  // Lost & Found + Safe
  tab_reports: { es: "Reportes", en: "Reports" },
  tab_safe: { es: "A salvo", en: "Safe" },
  subject_pet: { es: "Mascota", en: "Pet" },
  subject_person: { es: "Persona", en: "Person" },
  state_lost: { es: "Perdido", en: "Lost" },
  state_found: { es: "Encontrado", en: "Found" },
  last_seen: { es: "Visto por última vez", en: "Last seen" },
  safe_empty: { es: "Aún no hay avisos de «estoy a salvo».", en: "No 'I'm safe' check-ins yet." },
  safe_lead: {
    es: "Personas que avisaron que están a salvo. Solo nombre, ciudad y hora.",
    en: "People who checked in as safe. Name, city and time only.",
  },

  guide_emergency: { es: "Números de emergencia", en: "Emergency numbers" },
  guide_general: { es: "Emergencias", en: "Emergencies" },
  guide_tips: { es: "Consejos de seguridad", en: "Safety tips" },
  listen: { es: "Escuchar", en: "Listen" },
  stop: { es: "Detener", en: "Stop" },
  steps_heading: { es: "Paso a paso", en: "Step by step" },
  show_steps: { es: "Ver ilustración", en: "Show illustration" },
  hide_steps: { es: "Ocultar ilustración", en: "Hide illustration" },

  soon_title: { es: "En construcción", en: "Coming soon" },
  soon_body: {
    es: "Esta sección se está construyendo. Vuelve pronto.",
    en: "This section is being built. Check back soon.",
  },

  chart_fire_title: { es: "Concentración de incendios", en: "Fire concentration" },
  chart_fire_sub: { es: "Potencia radiada total por zona (MW)", en: "Total radiated power by area (MW)" },
  chart_quake_title: { es: "Actividad sísmica", en: "Seismic activity" },
  chart_quake_sub: { es: "Sismos por magnitud", en: "Earthquakes by magnitude" },
  other_areas: { es: "Otras zonas", en: "Other areas" },
  band_light: { es: "Leve (<3)", en: "Light (<3)" },
  band_moderate: { es: "Moderado (3–4.5)", en: "Moderate (3–4.5)" },
  band_strong: { es: "Fuerte (4.5–6)", en: "Strong (4.5–6)" },
  band_major: { es: "Mayor (≥6)", en: "Major (≥6)" },
  chart_empty: { es: "Sin datos para graficar.", en: "No data to chart." },

  moderators: { es: "Moderadores", en: "Moderators" },

  disclaimer: {
    es: "Kuna Ayuda no es un servicio de emergencia. En una emergencia, llama a los números oficiales de tu país.",
    en: "Kuna Ayuda is not an emergency service. In an emergency, call your country's official numbers.",
  },
} as const;

export type MsgKey = keyof typeof DICT;

interface I18n {
  lang: Lang;
  setLang: (l: Lang) => void;
  t: (key: MsgKey) => string;
}

const I18nContext = createContext<I18n | null>(null);

export function I18nProvider({ children }: { children: ReactNode }) {
  const [lang, setLangState] = useState<Lang>(initialLang);

  const setLang = useCallback((l: Lang) => {
    localStorage.setItem(STORAGE_KEY, l);
    document.documentElement.lang = l;
    setLangState(l);
  }, []);

  const t = useCallback((key: MsgKey) => DICT[key][lang], [lang]);

  const value = useMemo(() => ({ lang, setLang, t }), [lang, setLang, t]);
  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n(): I18n {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error("useI18n must be used within I18nProvider");
  return ctx;
}
