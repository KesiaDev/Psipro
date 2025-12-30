"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import BetaAccessModal from "./components/BetaAccessModal";

export default function LandingPage() {
  const router = useRouter();
  const [showBetaModal, setShowBetaModal] = useState(false);

  return (
    <div className="min-h-screen bg-psipro-background">
      {/* 1️⃣ HERO SECTION */}
      <section className="relative py-20 md:py-32 px-4 bg-gradient-to-br from-psipro-primary/5 via-psipro-background to-psipro-surface-elevated">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-12">
            <h1 className="text-4xl md:text-6xl font-bold text-psipro-text mb-6 tracking-tight leading-tight">
              PsiPro — organização, clareza e apoio para psicólogos
            </h1>
            <p className="text-xl md:text-2xl text-psipro-text-secondary mb-10 leading-relaxed max-w-3xl mx-auto">
              Agenda, pacientes e financeiro organizados em um só lugar — com uma visão clara do seu consultório.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={() => router.push("/dashboard")}
                className="px-8 py-4 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium text-lg"
              >
                Conhecer a plataforma
              </button>
              <button
                onClick={() => setShowBetaModal(true)}
                className="px-8 py-4 bg-psipro-surface border border-psipro-border text-psipro-text rounded-lg hover:bg-psipro-surface-elevated transition-colors font-medium text-lg"
              >
                Solicitar acesso
              </button>
            </div>
          </div>
          
          {/* Mock simples do dashboard */}
          <div className="mt-16 bg-psipro-surface-elevated border border-psipro-border rounded-lg p-8 max-w-4xl mx-auto">
            <div className="grid grid-cols-3 gap-4 mb-6">
              {[1, 2, 3].map((i) => (
                <div key={i} className="bg-psipro-background rounded-lg p-4 border border-psipro-border">
                  <div className="h-4 bg-psipro-surface rounded mb-2"></div>
                  <div className="h-8 bg-psipro-surface rounded w-2/3"></div>
                </div>
              ))}
            </div>
            <div className="bg-psipro-background rounded-lg p-6 border border-psipro-border">
              <div className="h-4 bg-psipro-surface rounded mb-4 w-1/3"></div>
              <div className="space-y-2">
                <div className="h-3 bg-psipro-surface rounded"></div>
                <div className="h-3 bg-psipro-surface rounded w-5/6"></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 2️⃣ O PROBLEMA */}
      <section className="py-20 px-4 bg-psipro-background">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-psipro-text mb-8 text-center">
            O dia a dia do psicólogo é mais complexo do que parece
          </h2>
          <div className="space-y-4 max-w-3xl mx-auto">
            {[
              "Agenda cheia, mas difícil de visualizar",
              "Financeiro espalhado e pouco claro",
              "Pacientes ativos sem acompanhamento organizado",
              "Falta de visão geral do consultório",
              "Sistemas que mais atrapalham do que ajudam",
            ].map((item, index) => (
              <div
                key={index}
                className="flex items-start gap-3 p-4 bg-psipro-surface-elevated rounded-lg border border-psipro-border"
              >
                <span className="text-psipro-text-secondary text-xl">•</span>
                <p className="text-psipro-text-secondary flex-1">{item}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 3️⃣ A PROPOSTA DO PSIPRO */}
      <section className="py-20 px-4 bg-psipro-surface-elevated">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-psipro-text mb-12 text-center">
            O PsiPro foi criado para organizar — não para complicar
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              {
                icon: "📅",
                title: "Agenda",
                description: "Visual simples do que acontece hoje e nos próximos dias.",
              },
              {
                icon: "👥",
                title: "Pacientes",
                description: "Dados organizados, sem excesso de burocracia.",
              },
              {
                icon: "💰",
                title: "Financeiro",
                description: "Cobranças automáticas e visão clara do mês.",
              },
              {
                icon: "💡",
                title: "Insights",
                description: "Observações inteligentes para apoiar suas decisões.",
              },
            ].map((card, index) => (
              <div
                key={index}
                className="bg-psipro-background rounded-lg border border-psipro-border p-6 hover:shadow-lg transition-all"
              >
                <div className="text-4xl mb-4">{card.icon}</div>
                <h3 className="text-xl font-semibold text-psipro-text mb-2">{card.title}</h3>
                <p className="text-sm text-psipro-text-secondary leading-relaxed">{card.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 4️⃣ COMO FUNCIONA (APP + WEB) */}
      <section className="py-20 px-4 bg-psipro-background">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-psipro-text mb-12 text-center">
            Como funciona
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
            {/* App */}
            <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-8">
              <div className="text-4xl mb-4">📱</div>
              <h3 className="text-2xl font-semibold text-psipro-text mb-4">App</h3>
              <p className="text-psipro-text-secondary mb-4">Ações do dia a dia:</p>
              <ul className="space-y-2 text-psipro-text-secondary">
                <li className="flex items-start gap-2">
                  <span>•</span>
                  <span>Agenda</span>
                </li>
                <li className="flex items-start gap-2">
                  <span>•</span>
                  <span>Confirmação de sessões</span>
                </li>
                <li className="flex items-start gap-2">
                  <span>•</span>
                  <span>Atendimento</span>
                </li>
              </ul>
            </div>

            {/* Web */}
            <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-8">
              <div className="text-4xl mb-4">💻</div>
              <h3 className="text-2xl font-semibold text-psipro-text mb-4">Web</h3>
              <p className="text-psipro-text-secondary mb-4">Visão estratégica:</p>
              <ul className="space-y-2 text-psipro-text-secondary">
                <li className="flex items-start gap-2">
                  <span>•</span>
                  <span>Dashboard</span>
                </li>
                <li className="flex items-start gap-2">
                  <span>•</span>
                  <span>Financeiro</span>
                </li>
                <li className="flex items-start gap-2">
                  <span>•</span>
                  <span>Gestão da clínica</span>
                </li>
                <li className="flex items-start gap-2">
                  <span>•</span>
                  <span>Insights</span>
                </li>
              </ul>
            </div>
          </div>
          <div className="mt-12 text-center">
            <p className="text-lg text-psipro-text-secondary">
              <strong className="text-psipro-text">Tudo sincronizado.</strong> O que você faz em um aparece no outro.
            </p>
          </div>
        </div>
      </section>

      {/* 5️⃣ DIFERENCIAL (INSIGHTS) */}
      <section className="py-20 px-4 bg-psipro-surface-elevated">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-psipro-text mb-6 text-center">
            Um sistema que observa com você
          </h2>
          <p className="text-lg text-psipro-text-secondary mb-12 text-center leading-relaxed max-w-3xl mx-auto">
            O PsiPro analisa dados administrativos e apresenta observações simples para ajudar você a entender sua rotina — sem julgamentos e sem invadir o cuidado clínico.
          </p>
          
          {/* Exemplos de insights */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
            {[
              "Alguns pacientes ativos não têm sessões futuras.",
              "Sua receita deste mês está abaixo da média.",
              "Há muitos horários vagos nesta semana.",
            ].map((insight, index) => (
              <div
                key={index}
                className="bg-psipro-background rounded-lg border border-psipro-border p-4"
              >
                <p className="text-sm text-psipro-text-secondary">{insight}</p>
              </div>
            ))}
          </div>

          {/* Disclaimer */}
          <div className="bg-psipro-warning/10 border border-psipro-warning/20 rounded-lg p-4 text-center">
            <p className="text-sm text-psipro-text-secondary">
              <strong className="text-psipro-text">O PsiPro não realiza diagnósticos nem recomendações clínicas.</strong>
            </p>
          </div>
        </div>
      </section>

      {/* 6️⃣ PARA QUEM É */}
      <section className="py-20 px-4 bg-psipro-background">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-3xl md:text-4xl font-bold text-psipro-text mb-12 text-center">
            Para quem é
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {[
              {
                title: "Psicólogos autônomos",
                description: "Profissionais que querem organizar sua prática sem complicação.",
              },
              {
                title: "Clínicas com múltiplos profissionais",
                description: "Equipes que precisam de organização compartilhada.",
              },
              {
                title: "Quem quer organização e clareza",
                description: "Profissionais que buscam simplificar a gestão.",
              },
              {
                title: "Quem prefere sistemas simples e humanos",
                description: "Pessoas que valorizam simplicidade e usabilidade.",
              },
            ].map((card, index) => (
              <div
                key={index}
                className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6"
              >
                <h3 className="text-xl font-semibold text-psipro-text mb-2">{card.title}</h3>
                <p className="text-sm text-psipro-text-secondary leading-relaxed">{card.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* 7️⃣ CTA FINAL */}
      <section id="beta" className="py-24 px-4 bg-gradient-to-br from-psipro-primary/10 to-psipro-background">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-3xl md:text-5xl font-bold text-psipro-text mb-6">
            Comece com clareza
          </h2>
          <p className="text-lg text-psipro-text-secondary mb-10 leading-relaxed max-w-2xl mx-auto">
            O PsiPro está em fase inicial. Estamos convidando psicólogos que queiram acompanhar a evolução da plataforma.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button
              onClick={() => setShowBetaModal(true)}
              className="px-8 py-4 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium text-lg"
            >
              Solicitar acesso
            </button>
            <button
              onClick={() => router.push("/dashboard")}
              className="px-8 py-4 bg-psipro-surface border border-psipro-border text-psipro-text rounded-lg hover:bg-psipro-surface-elevated transition-colors font-medium text-lg"
            >
              Ver como funciona
            </button>
          </div>
        </div>
      </section>

      {/* 8️⃣ FOOTER SIMPLES */}
      <footer className="py-12 px-4 bg-psipro-surface border-t border-psipro-border">
        <div className="max-w-6xl mx-auto">
          <div className="flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="text-center md:text-left">
              <h3 className="text-2xl font-bold text-psipro-text mb-2">PsiPro</h3>
              <p className="text-sm text-psipro-text-secondary">
                Gestão inteligente para psicólogos
              </p>
            </div>
            <div className="text-center md:text-right">
              <p className="text-sm text-psipro-text-secondary mb-2">
                Produto em desenvolvimento
              </p>
              <p className="text-sm text-psipro-text-secondary">
                contato@psipro.com.br
              </p>
            </div>
          </div>
        </div>
      </footer>

      {/* Modal de Solicitação de Acesso */}
      <BetaAccessModal isOpen={showBetaModal} onClose={() => setShowBetaModal(false)} />
    </div>
  );
}
