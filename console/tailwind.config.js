/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        // Kuna brand teal.
        primary: { DEFAULT: "#0F5E66", accent: "#0a464c", muted: "#e6f0f1" },
        danger: { DEFAULT: "#b3261e", muted: "#fbeae9" },
      },
    },
  },
  plugins: [],
};
