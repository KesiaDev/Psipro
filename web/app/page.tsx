"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function HomePage() {
  const router = useRouter();
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    setIsVisible(true);
  }, []);

  return (
    <div className="min-h-screen relative overflow-hidden">
      {/* Botão WhatsApp Flutuante */}
      <a
        href="https://wa.me/5511999999999?text=Olá!%20Gostaria%20de%20conhecer%20o%20PsiPro"
        target="_blank"
        rel="noopener noreferrer"
        className="fixed bottom-6 right-6 z-50 w-14 h-14 bg-[#25D366] rounded-full flex items-center justify-center shadow-lg hover:shadow-xl hover:scale-110 transition-all duration-300 group"
        aria-label="Fale conosco no WhatsApp"
      >
        <svg
          className="w-7 h-7 text-white"
          fill="currentColor"
          viewBox="0 0 24 24"
        >
          <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.625.712.227 1.36.195 1.871.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347m-5.421 7.403h-.004a9.87 9.87 0 01-5.031-1.378l-.361-.214-3.741.982.998-3.648-.235-.374a9.86 9.86 0 01-1.51-5.26c.001-5.45 4.436-9.884 9.888-9.884 2.64 0 5.122 1.03 6.988 2.898a9.825 9.825 0 012.893 6.994c-.003 5.45-4.437 9.884-9.885 9.884m8.413-18.297A11.815 11.815 0 0012.05 0C5.495 0 .16 5.335.157 11.892c0 2.096.547 4.142 1.588 5.945L.057 24l6.305-1.654a11.882 11.882 0 005.683 1.448h.005c6.554 0 11.89-5.335 11.893-11.893a11.821 11.821 0 00-3.48-8.413Z" />
        </svg>
        <span className="absolute -top-12 right-0 bg-psipro-text text-white text-xs px-3 py-1.5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap shadow-lg">
          Fale conosco
        </span>
      </a>

      {/* Hero Section com Gradiente */}
      <section className="relative py-24 md:py-32 px-4 bg-gradient-to-br from-psipro-primary/10 via-psipro-background to-psipro-surface-elevated overflow-hidden">
        {/* Elementos decorativos de fundo */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute -top-40 -right-40 w-80 h-80 bg-psipro-primary/5 rounded-full blur-3xl"></div>
          <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-psipro-primary/5 rounded-full blur-3xl"></div>
        </div>

        <div className={`max-w-5xl mx-auto text-center relative z-10 transition-all duration-1000 ${isVisible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10'}`}>
          <h1 className="text-5xl md:text-7xl font-bold text-psipro-text mb-8 tracking-tight leading-tight animate-fade-in">
            O PsiPro organiza sua prática para você cuidar do que importa.
          </h1>
          <p className="text-xl md:text-2xl text-psipro-text-secondary mb-12 leading-relaxed animate-fade-in-delay">
            Um app para o dia a dia clínico.
            <br />
            Uma plataforma web para gestão e clareza.
            <br />
            <span className="text-psipro-primary font-semibold">Tudo trabalhando junto.</span>
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center animate-fade-in-delay-2">
            <button
              onClick={() => router.push("/dashboard")}
              className="px-10 py-5 text-lg font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all shadow-lg hover:shadow-xl hover:scale-105 transform duration-300"
            >
              Conhecer o PsiPro
            </button>
            <button
              onClick={() => {
                const element = document.getElementById("como-funciona");
                element?.scrollIntoView({ behavior: "smooth" });
              }}
              className="px-10 py-5 text-lg font-medium text-psipro-text-secondary bg-psipro-surface border-2 border-psipro-border rounded-lg hover:bg-psipro-surface-elevated hover:border-psipro-primary transition-all shadow-lg hover:shadow-xl hover:scale-105 transform duration-300"
            >
              Ver como funciona
            </button>
          </div>
        </div>
      </section>

      {/* O Problema - com animação */}
      <section className="py-20 px-4 bg-psipro-background relative">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold text-psipro-text mb-8 text-center">
            A rotina clínica já é complexa o suficiente.
          </h2>
          <p className="text-xl text-psipro-text-secondary mb-12 text-center leading-relaxed max-w-3xl mx-auto">
            Entre atendimentos, agenda, cobranças e organização, muitos psicólogos acabam usando
            planilhas, anotações soltas ou vários sistemas que não conversam entre si.
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl mx-auto">
            {[
              "Falta de clareza financeira",
              "Agenda difícil de visualizar",
              "Dados espalhados",
              "Pouco tempo para analisar",
            ].map((item, index) => (
              <div
                key={index}
                className="bg-psipro-surface-elevated rounded-xl border border-psipro-border p-6 hover:border-psipro-primary/50 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1"
              >
                <p className="text-psipro-text-secondary flex items-center gap-3">
                  <span className="text-psipro-primary text-xl">•</span>
                  {item}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* A Solução - Cards com hover */}
      <section className="py-20 px-4 bg-gradient-to-b from-psipro-surface-elevated to-psipro-background">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold text-psipro-text mb-6 text-center">
            O PsiPro foi criado para simplificar.
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-10">
            {[
              {
                icon: "📱",
                title: "App para atender, registrar e seguir o dia",
                description: "Use no celular durante os atendimentos. Rápido, simples e sempre disponível.",
                color: "from-psipro-primary/10 to-psipro-primary/5",
              },
              {
                icon: "💻",
                title: "Web para acompanhar, organizar e analisar",
                description: "Veja tudo com mais clareza no computador. Gestão completa e visão ampla.",
                color: "from-psipro-success/10 to-psipro-success/5",
              },
              {
                icon: "🧠",
                title: "IA para ajudar a enxergar padrões",
                description: "Insights inteligentes sobre sua prática, sem substituir seu julgamento.",
                color: "from-psipro-warning/10 to-psipro-warning/5",
              },
            ].map((card, index) => (
              <div
                key={index}
                className="bg-psipro-background rounded-2xl border border-psipro-border p-8 text-center hover:shadow-2xl transition-all duration-500 transform hover:-translate-y-2 hover:border-psipro-primary/50 group relative overflow-hidden"
              >
                <div className={`absolute inset-0 bg-gradient-to-br ${card.color} opacity-0 group-hover:opacity-100 transition-opacity duration-500`}></div>
                <div className="relative z-10">
                  <div className="text-6xl mb-6 transform group-hover:scale-110 transition-transform duration-300">
                    {card.icon}
                  </div>
                  <h3 className="text-xl font-semibold text-psipro-text mb-3">{card.title}</h3>
                  <p className="text-sm text-psipro-text-secondary leading-relaxed">{card.description}</p>
                </div>
              </div>
            ))}
          </div>
          <p className="text-center text-xl text-psipro-text-secondary font-medium">
            O PsiPro funciona como um assistente, não como um sistema complicado.
          </p>
        </div>
      </section>

      {/* Como Funciona - com animação de scroll */}
      <section id="como-funciona" className="py-20 px-4 bg-psipro-background">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold text-psipro-text mb-16 text-center">
            Como funciona
          </h2>
          <div className="space-y-12">
            {[
              {
                step: "1",
                title: "Atenda seus pacientes pelo app",
                description:
                  "Use o app Android durante os atendimentos. Agende, registre sessões e pagamentos de forma natural, como você já faz.",
              },
              {
                step: "2",
                title: "Registre sessões e pagamentos normalmente",
                description:
                  "Tudo que você faz no app fica salvo localmente e sincroniza automaticamente quando há internet. Você não precisa mudar nada.",
              },
              {
                step: "3",
                title: "Veja tudo organizado e analisado na web",
                description:
                  "Na plataforma web, você acompanha agenda, financeiro e pacientes com visão ampla. Tudo sincronizado automaticamente do app.",
              },
            ].map((item, index) => (
              <div
                key={index}
                className="flex items-start gap-8 group hover:bg-psipro-surface-elevated p-6 rounded-xl transition-all duration-300"
              >
                <div className="flex-shrink-0 w-16 h-16 rounded-2xl bg-gradient-to-br from-psipro-primary to-psipro-primary-dark flex items-center justify-center text-psipro-text font-bold text-2xl shadow-lg group-hover:scale-110 transition-transform duration-300">
                  {item.step}
                </div>
                <div className="flex-1">
                  <h3 className="text-2xl font-semibold text-psipro-text mb-3">{item.title}</h3>
                  <p className="text-psipro-text-secondary leading-relaxed text-lg">{item.description}</p>
                </div>
              </div>
            ))}
          </div>
          <div className="mt-16 bg-gradient-to-r from-psipro-primary/10 to-psipro-primary/5 rounded-2xl border border-psipro-primary/20 p-8 text-center">
            <p className="text-xl text-psipro-text-secondary">
              Você não muda sua forma de trabalhar.
              <br />
              <strong className="text-psipro-text text-2xl">O PsiPro se adapta a você.</strong>
            </p>
          </div>
        </div>
      </section>

      {/* Diferencial - IA com Ética */}
      <section className="py-20 px-4 bg-gradient-to-b from-psipro-background to-psipro-surface-elevated">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold text-psipro-text mb-6 text-center">
            Inteligência que respeita sua prática.
          </h2>
          <p className="text-xl text-psipro-text-secondary mb-12 text-center leading-relaxed max-w-3xl mx-auto">
            Os insights do PsiPro ajudam a identificar padrões de agenda, frequência e financeiro,
            sem diagnosticar pacientes e sem substituir o julgamento profissional.
          </p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl mx-auto">
            {[
              "Você tem muitos horários vagos às quartas.",
              "Sessões semanais geram maior previsibilidade.",
              "Cancelamentos impactaram sua receita este mês.",
            ].map((insight, index) => (
              <div
                key={index}
                className="bg-psipro-background rounded-xl border border-psipro-border p-6 hover:border-psipro-primary/50 hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1 relative overflow-hidden group"
              >
                <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-psipro-primary to-psipro-primary-dark opacity-0 group-hover:opacity-100 transition-opacity"></div>
                <p className="text-sm text-psipro-text-secondary relative z-10">{insight}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Para Quem É */}
      <section className="py-20 px-4 bg-psipro-background">
        <div className="max-w-6xl mx-auto">
          <h2 className="text-4xl md:text-5xl font-bold text-psipro-text mb-16 text-center">
            Para quem é
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {[
              {
                title: "Psicólogos autônomos",
                description:
                  "Profissionais que querem organizar sua prática sem complicação, focando no que realmente importa: o atendimento.",
              },
              {
                title: "Profissionais em crescimento",
                description:
                  "Quem está expandindo a prática e precisa de clareza sobre agenda, pacientes e receita para tomar melhores decisões.",
              },
              {
                title: "Clínicas pequenas",
                description:
                  "Equipes pequenas que precisam de organização compartilhada e visão consolidada da prática clínica.",
              },
              {
                title: "Quem quer mais organização e clareza",
                description:
                  "Qualquer profissional que busca simplificar a gestão e ganhar tempo para o que realmente importa.",
              },
            ].map((card, index) => (
              <div
                key={index}
                className="bg-psipro-surface-elevated rounded-2xl border border-psipro-border p-8 hover:shadow-2xl transition-all duration-500 transform hover:-translate-y-2 hover:border-psipro-primary/50 group"
              >
                <h3 className="text-2xl font-semibold text-psipro-text mb-4 group-hover:text-psipro-primary transition-colors">
                  {card.title}
                </h3>
                <p className="text-psipro-text-secondary leading-relaxed">{card.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Final - com gradiente */}
      <section className="py-24 px-4 bg-gradient-to-br from-psipro-primary/20 via-psipro-primary/10 to-psipro-background relative overflow-hidden">
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute top-0 right-0 w-96 h-96 bg-psipro-primary/10 rounded-full blur-3xl"></div>
          <div className="absolute bottom-0 left-0 w-96 h-96 bg-psipro-primary/10 rounded-full blur-3xl"></div>
        </div>
        <div className="max-w-4xl mx-auto text-center relative z-10">
          <h2 className="text-4xl md:text-6xl font-bold text-psipro-text mb-8">
            Trabalhe com mais clareza e menos esforço.
          </h2>
          <div className="flex flex-col sm:flex-row gap-4 justify-center mb-6">
            <button
              onClick={() => router.push("/dashboard")}
              className="px-10 py-5 text-lg font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all shadow-xl hover:shadow-2xl hover:scale-105 transform duration-300"
            >
              Quero conhecer o PsiPro
            </button>
            <button
              onClick={() => router.push("/dashboard")}
              className="px-10 py-5 text-lg font-medium text-psipro-text-secondary bg-psipro-surface border-2 border-psipro-border rounded-lg hover:bg-psipro-surface-elevated hover:border-psipro-primary transition-all shadow-xl hover:shadow-2xl hover:scale-105 transform duration-300"
            >
              Entrar na plataforma
            </button>
          </div>
          <p className="text-sm text-psipro-text-muted">Sem cartão. Sem compromisso.</p>
        </div>
      </section>

      {/* Rodapé */}
      <footer className="py-16 px-4 bg-psipro-surface border-t border-psipro-border">
        <div className="max-w-6xl mx-auto">
          <div className="flex flex-col md:flex-row items-center justify-between gap-8">
            <div>
              <h3 className="text-2xl font-bold text-psipro-text mb-3">PsiPro</h3>
              <p className="text-sm text-psipro-text-secondary max-w-md leading-relaxed">
                Gestão inteligente para psicólogos. Organize sua prática, ganhe clareza e foque no
                que importa: cuidar de pessoas.
              </p>
            </div>
            <div className="text-sm text-psipro-text-muted text-center md:text-right">
              <p>© 2025 PsiPro. Todos os direitos reservados.</p>
              <div className="mt-3 flex gap-6 justify-center md:justify-end">
                <Link href="#" className="hover:text-psipro-text-secondary transition-colors">
                  Privacidade
                </Link>
                <Link href="#" className="hover:text-psipro-text-secondary transition-colors">
                  Termos
                </Link>
              </div>
            </div>
          </div>
        </div>
      </footer>

      <style jsx>{`
        @keyframes fade-in {
          from {
            opacity: 0;
            transform: translateY(20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        .animate-fade-in {
          animation: fade-in 0.8s ease-out;
        }

        .animate-fade-in-delay {
          animation: fade-in 0.8s ease-out 0.2s both;
        }

        .animate-fade-in-delay-2 {
          animation: fade-in 0.8s ease-out 0.4s both;
        }
      `}</style>
    </div>
  );
}
