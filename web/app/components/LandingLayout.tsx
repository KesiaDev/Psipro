"use client";

import { usePathname } from "next/navigation";
import Header from "./Header";
import Sidebar from "./Sidebar";
import OnboardingModal from "./OnboardingModal";

export default function LandingLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isHomePage = pathname === "/";

  if (isHomePage) {
    return (
      <>
        {children}
        <OnboardingModal />
      </>
    );
  }

  return (
    <>
      <Header />
      <Sidebar />
      <main className="ml-64 mt-16 min-h-[calc(100vh-4rem)] p-8">
        <div className="max-w-7xl">{children}</div>
      </main>
      <OnboardingModal />
    </>
  );
}

