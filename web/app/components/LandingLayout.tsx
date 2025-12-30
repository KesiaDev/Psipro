"use client";

import { usePathname } from "next/navigation";
import Header from "./Header";
import Sidebar from "./Sidebar";
import BetaAccessGate from "./BetaAccessGate";

export default function LandingLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isHomePage = pathname === "/";
  const isBetaPage = pathname === "/beta";
  
  // Páginas públicas (não precisam de verificação de acesso)
  const isPublicPage = isHomePage || isBetaPage;

  if (isHomePage || isBetaPage) {
    return <>{children}</>;
  }

  // Páginas autenticadas: proteger com BetaAccessGate
  return (
    <BetaAccessGate>
      <Header />
      <Sidebar />
      <main className="ml-64 mt-16 min-h-[calc(100vh-4rem)] p-8">
        <div className="max-w-7xl">{children}</div>
      </main>
    </BetaAccessGate>
  );
}


