import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { DEFAULT_COUNTRY, type CountryCode } from "./content/countries";

// Selected country (drives every ?country= call). Persisted so a reload keeps the choice.

const STORAGE_KEY = "kuna_country";

function initialCountry(): CountryCode {
  const saved = localStorage.getItem(STORAGE_KEY);
  return (["CO", "ID", "ES", "IT", "PE"] as CountryCode[]).includes(saved as CountryCode)
    ? (saved as CountryCode)
    : DEFAULT_COUNTRY;
}

interface CountryState {
  country: CountryCode;
  setCountry: (c: CountryCode) => void;
}

const Ctx = createContext<CountryState | null>(null);

export function CountryProvider({ children }: { children: ReactNode }) {
  const [country, setCountryState] = useState<CountryCode>(initialCountry);
  const value = useMemo<CountryState>(
    () => ({
      country,
      setCountry: (c) => {
        localStorage.setItem(STORAGE_KEY, c);
        setCountryState(c);
      },
    }),
    [country],
  );
  return <Ctx.Provider value={value}>{children}</Ctx.Provider>;
}

export function useCountry(): CountryState {
  const ctx = useContext(Ctx);
  if (!ctx) throw new Error("useCountry must be used within CountryProvider");
  return ctx;
}
