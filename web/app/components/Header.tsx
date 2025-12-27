"use client";

import Image from "next/image";
import Link from "next/link";
import { useTheme } from "@/app/contexts/ThemeContext";

export default function Header() {
  const { theme, toggleTheme } = useTheme();

  return (
    <header className="fixed top-0 left-0 right-0 h-16 bg-psipro-surface-elevated border-b border-psipro-border z-40 flex items-center px-6 shadow-sm">
      <div className="flex items-center gap-3 min-w-0">
        <Link href="/dashboard" className="flex items-center shrink-0">
          <Image
            src="/brand/logo-psipro.png"
            alt="PsiPro - Gestão Inteligente para Psicólogos"
            width={280}
            height={112}
            className="h-[56px] sm:h-[48px] md:h-[56px] w-auto"
            priority
            unoptimized
          />
        </Link>
      </div>
      <div className="ml-auto flex items-center gap-4">
        {/* Toggle de tema */}
        <button
          onClick={toggleTheme}
          className="p-2 rounded-lg hover:bg-psipro-surface transition-all duration-200 flex items-center justify-center"
          aria-label={theme === "dark" ? "Alternar para modo claro" : "Alternar para modo escuro"}
          title={theme === "dark" ? "Modo escuro ativo - Clique para modo claro" : "Modo claro ativo - Clique para modo escuro"}
        >
          {theme === "dark" ? (
            <span className="text-xl transition-transform duration-200">🌙</span>
          ) : (
            <span className="text-xl transition-transform duration-200">☀️</span>
          )}
        </button>
        <div className="h-9 w-9 rounded-full bg-psipro-primary border border-psipro-primary-dark flex items-center justify-center">
          <span className="text-psipro-text text-sm font-medium">U</span>
        </div>
      </div>
    </header>
  );
}

