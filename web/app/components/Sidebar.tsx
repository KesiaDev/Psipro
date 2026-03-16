"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import ClinicSelector from "./ClinicSelector";

const navigation = [
  { name: "Dashboard", href: "/dashboard", icon: "📊" },
  { name: "Pacientes", href: "/pacientes", icon: "👥" },
  { name: "Agenda", href: "/agenda", icon: "📅" },
  { name: "Financeiro", href: "/financeiro", icon: "💰" },
  { name: "Relatórios", href: "/relatorios", icon: "📈" },
  { name: "Clínicas", href: "/clinica", icon: "🏥" },
  { name: "🧪 Teste", href: "/test", icon: "🧪" },
];

type SidebarProps = {
  isOpen?: boolean;
  onClose?: () => void;
  onNavigate?: () => void;
};

export default function Sidebar({ isOpen = false, onClose, onNavigate }: SidebarProps) {
  const pathname = usePathname();

  const navContent = (
    <nav className="p-4 space-y-2">
      {navigation.map((item) => {
        const isActive = pathname === item.href;
        return (
          <Link
            key={item.href}
            href={item.href}
            onClick={onNavigate}
            className={`
              flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-200
              ${
                isActive
                  ? "bg-psipro-primary/20 text-psipro-primary shadow-sm border border-psipro-primary/30"
                  : "text-psipro-text-secondary hover:bg-psipro-surface-elevated hover:text-psipro-text"
              }
            `}
          >
            <span className="text-lg">{item.icon}</span>
            <span className="tracking-wide">{item.name}</span>
          </Link>
        );
      })}
    </nav>
  );

  // Mobile/Tablet: drawer com overlay. Desktop (xl+): sidebar fixa.
  return (
    <>
      {/* Overlay em mobile (clique fecha) */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-40 xl:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}
      <aside
        className={`
          fixed left-0 top-14 sm:top-16 bottom-0 w-64 bg-psipro-surface border-r border-psipro-border z-50 overflow-y-auto
          transition-transform duration-200 ease-out will-change-transform
          xl:translate-x-0 xl:z-30 xl:will-change-auto
          ${isOpen ? "translate-x-0" : "-translate-x-full xl:translate-x-0"}
        `}
      >
        <div className="p-4 border-b border-psipro-border xl:hidden space-y-3">
          <div className="flex items-center justify-between">
            <span className="font-semibold text-psipro-text">Menu</span>
            <button
              onClick={onClose}
              className="p-2 rounded-lg hover:bg-psipro-surface-elevated text-psipro-text-muted"
              aria-label="Fechar menu"
            >
              ✕
            </button>
          </div>
          <div className="sm:hidden">
            <ClinicSelector />
          </div>
        </div>
        {navContent}
      </aside>
    </>
  );
}

