/**
 * ⚠️ ARQUIVO CRÍTICO - INTEGRAÇÃO BACKEND
 *
 * Este arquivo contém lógica essencial de integração com API,
 * autenticação ou variáveis de ambiente.
 *
 * NÃO alterar estrutura, headers, interceptors ou contratos de API
 * durante modernização visual.
 *
 * Qualquer alteração pode quebrar produção.
 */

"use client";

import { createContext, useContext, useEffect, useState } from "react";

type Theme = "light" | "dark";

interface ThemeContextType {
  theme: Theme;
  toggleTheme: () => void;
  setTheme: (theme: Theme) => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [theme, setThemeState] = useState<Theme>("dark");
  const [mounted, setMounted] = useState(false);

  const applyTheme = (newTheme: Theme) => {
    if (typeof window !== "undefined") {
      const root = document.documentElement;
      if (newTheme === "dark") {
        root.classList.add("dark");
      } else {
        root.classList.remove("dark");
      }
    }
  };

  const setTheme = (newTheme: Theme) => {
    setThemeState(newTheme);
    applyTheme(newTheme);
    if (typeof window !== "undefined") {
      localStorage.setItem("psipro-theme", newTheme);
    }
  };

  const toggleTheme = () => {
    const newTheme: Theme = theme === "dark" ? "light" : "dark";
    setTheme(newTheme);
  };

  useEffect(() => {
    // Apenas no cliente
    setMounted(true);

    // Verificar localStorage primeiro
    const savedTheme = localStorage.getItem("psipro-theme") as Theme | null;
    
    if (savedTheme && (savedTheme === "light" || savedTheme === "dark")) {
      setThemeState(savedTheme);
      applyTheme(savedTheme);
    } else {
      // Se não houver preferência salva, detectar preferência do sistema
      const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
      const systemTheme: Theme = prefersDark ? "dark" : "light";
      setThemeState(systemTheme);
      applyTheme(systemTheme);
      
      // Listener para mudanças na preferência do sistema (apenas se não houver preferência salva)
      const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
      const handleChange = (e: MediaQueryListEvent) => {
        if (!localStorage.getItem("psipro-theme")) {
          const newSystemTheme: Theme = e.matches ? "dark" : "light";
          setThemeState(newSystemTheme);
          applyTheme(newSystemTheme);
        }
      };
      
      mediaQuery.addEventListener("change", handleChange);
      return () => mediaQuery.removeEventListener("change", handleChange);
    }
  }, []);

  // Sempre fornecer o contexto, mesmo durante SSR
  // O valor padrão será usado até o useEffect executar
  return (
    <ThemeContext.Provider value={{ theme, toggleTheme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error("useTheme must be used within a ThemeProvider");
  }
  return context;
}

