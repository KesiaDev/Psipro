import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { ThemeProvider } from "./contexts/ThemeContext";
import { ClinicProvider } from "./contexts/ClinicContext";
import { ToastProvider } from "./contexts/ToastContext";
import { OnboardingProvider } from "./contexts/OnboardingContext";
import LandingLayout from "./components/LandingLayout";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "PsiPro — Gestão Inteligente para Psicólogos",
  description: "Plataforma completa para agenda, pacientes e financeiro",
  icons: {
    icon: [
      { url: "/brand/icon-psipro.svg", type: "image/svg+xml" },
      { url: "/favicon.ico", sizes: "any" },
    ],
    apple: [
      { url: "/brand/icon-psipro.svg", type: "image/svg+xml" },
    ],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `
              (function() {
                try {
                  var theme = localStorage.getItem('psipro-theme');
                  if (theme === 'light' || theme === 'dark') {
                    if (theme === 'dark') {
                      document.documentElement.classList.add('dark');
                    } else {
                      document.documentElement.classList.remove('dark');
                    }
                  } else {
                    var prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
                    if (prefersDark) {
                      document.documentElement.classList.add('dark');
                    } else {
                      document.documentElement.classList.remove('dark');
                    }
                  }
                } catch (e) {}
              })();
            `,
          }}
        />
      </head>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-psipro-background text-psipro-text transition-colors duration-200`}
      >
        <ThemeProvider>
          <ToastProvider>
            <OnboardingProvider>
              <ClinicProvider>
                <LandingLayout>{children}</LandingLayout>
              </ClinicProvider>
            </OnboardingProvider>
          </ToastProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
