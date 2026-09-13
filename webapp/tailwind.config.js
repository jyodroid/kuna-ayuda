/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        // Kuna brand teal (matches the app + console).
        primary: { DEFAULT: "#0F5E66", accent: "#0a464c", muted: "#e6f0f1" },
        // Severity accents — used ONLY alongside a text label / icon, never color alone.
        danger: { DEFAULT: "#b3261e", muted: "#fbeae9" },
        warn: { DEFAULT: "#c15b12", muted: "#fbeee2" },
      },
    },
  },
  plugins: [],
};
