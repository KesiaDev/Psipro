"use client";

import { useState } from "react";
import { usePathname } from "next/navigation";
import Header from "./Header";
import Sidebar from "./Sidebar";
import AuthGuard from "./AuthGuard";
import BetaAccessGate from "./BetaAccessGate";

export default function LandingLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const isHomePage = pathname === "/";
  const isBetaPage = pathname === "/beta";
  const isAuthPage = pathname === "/login" || pathname === "/register";
  const isHandoffPage = pathname === "/handoff";

  // Páginas públicas (não precisam de autenticação)
  if (isHomePage || isBetaPage || isAuthPage || isHandoffPage) {
    return <>{children}</>;
  }

  // Páginas autenticadas: proteger com AuthGuard e BetaAccessGate
  return (
    <AuthGuard>
      <BetaAccessGate>
        <Header onMenuClick={() => setSidebarOpen(true)} />
        <Sidebar
          isOpen={sidebarOpen}
          onClose={() => setSidebarOpen(false)}
          onNavigate={() => setSidebarOpen(false)}
        />
        <main
          className="mt-14 sm:mt-16 min-h-[calc(100vh-3.5rem)] sm:min-h-[calc(100vh-4rem)] p-4 sm:p-6 md:p-8
            xl:ml-64 transition-[margin] duration-200 overflow-x-hidden min-w-0"
        >
          <div className="max-w-7xl mx-auto">{children}</div>
        </main>
      </BetaAccessGate>
    </AuthGuard>
  );
}
