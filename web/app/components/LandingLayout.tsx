"use client";

import { usePathname } from "next/navigation";
import Header from "./Header";
import Sidebar from "./Sidebar";
import AuthGuard from "./AuthGuard";
import BetaAccessGate from "./BetaAccessGate";

export default function LandingLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
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
        <Header />
        <Sidebar />
        <main className="ml-64 mt-16 min-h-[calc(100vh-4rem)] p-8">
          <div className="max-w-7xl">{children}</div>
        </main>
      </BetaAccessGate>
    </AuthGuard>
  );
}
