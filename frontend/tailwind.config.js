/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        // Module accents — keep in sync with src/lib/tokens.ts.
        finance: "#1E6F5C",
        hr: "#5B5BD6",
        sales: "#C2410C",
        projects: "#0E7490",
        // Semantic colors.
        positive: "#15803D",
        negative: "#B91C1C",
        warning: "#B45309",
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [],
};
