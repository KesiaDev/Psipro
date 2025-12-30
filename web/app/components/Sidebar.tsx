"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

const navigation = [
  { name: "Dashboard", href: "/dashboard", icon: "📊" },
  { name: "Pacientes", href: "/pacientes", icon: "👥" },
  { name: "Agenda", href: "/agenda", icon: "📅" },
  { name: "Financeiro", href: "/financeiro", icon: "💰" },
  { name: "Clínicas", href: "/clinica", icon: "🏥" },
  { name: "🧪 Teste", href: "/test", icon: "🧪" },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="fixed left-0 top-16 bottom-0 w-64 bg-psipro-surface border-r border-psipro-border z-30 overflow-y-auto">
      <nav className="p-4 space-y-2">
        {navigation.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
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
    </aside>
  );
}

