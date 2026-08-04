import { createContext, useContext, useEffect, useState } from "react";

// Diaxeirizetai to "light" / "dark" theme. Vazei ena attribute data-theme sto <html>,
// kai to CSS mas (index.css) allazei ola ta xromata mesa apo tis CSS variables.
const ThemeContext = createContext(null);

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => {
    const saved = localStorage.getItem("maintrack_theme");
    if (saved) return saved;
    // An o xristis den exei epilexei, akolouthoume ti rythmisi tou leitourgikou tou.
    return window.matchMedia?.("(prefers-color-scheme: dark)").matches ? "dark" : "light";
  });

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("maintrack_theme", theme);
  }, [theme]);

  function toggleTheme() {
    setTheme((t) => (t === "dark" ? "light" : "dark"));
  }

  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, isDark: theme === "dark" }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  return useContext(ThemeContext);
}
