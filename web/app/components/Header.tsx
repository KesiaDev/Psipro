"use client";

import { useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useTheme } from "@/app/contexts/ThemeContext";
import { useAuth } from "@/app/contexts/AuthContext";
import ClinicSelector from "./ClinicSelector";

export default function Header() {
  const { theme, toggleTheme } = useTheme();
  const { user, logout } = useAuth();
  const router = useRouter();
  const [showMenu, setShowMenu] = useState(false);

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  // Obter inicial do nome do usuário
  const getUserInitial = () => {
    if (user?.fullName) {
      return user.fullName.charAt(0).toUpperCase();
    }
    if (user?.email) {
      return user.email.charAt(0).toUpperCase();
    }
    return "U";
  };

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
        <ClinicSelector />
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
        
        {/* Menu do Usuário */}
        <div className="relative">
          <button
            onClick={() => setShowMenu(!showMenu)}
            className="h-9 w-9 rounded-full bg-psipro-primary border border-psipro-primary-dark flex items-center justify-center hover:bg-psipro-primary-dark transition-colors"
            aria-label="Menu do usuário"
          >
            <span className="text-white text-sm font-medium">{getUserInitial()}</span>
          </button>

          {/* Dropdown Menu */}
          {showMenu && (
            <>
              <div
                className="fixed inset-0 z-10"
                onClick={() => setShowMenu(false)}
              />
              <div className="absolute right-0 mt-2 w-48 bg-psipro-surface-elevated border border-psipro-border rounded-lg shadow-lg z-20 py-1">
                <div className="px-4 py-2 border-b border-psipro-border">
                  <p className="text-sm font-medium text-psipro-text">{user?.fullName || "Usuário"}</p>
                  <p className="text-xs text-psipro-text-secondary truncate">{user?.email}</p>
                </div>
                <button
                  onClick={handleLogout}
                  className="w-full text-left px-4 py-2 text-sm text-psipro-text hover:bg-psipro-surface transition-colors"
                >
                  Sair
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

