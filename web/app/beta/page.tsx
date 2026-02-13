"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import BetaAccessForm from "../components/BetaAccessForm";
import { useToast } from "../contexts/ToastContext";

/**
 * Página de Convite Beta do PsiPro
 * 
 * Foco em clareza, acolhimento e controle de acesso.
 * Sem venda forçada, sem planos, sem preços.
 */
export default function BetaInvitePage() {
  const router = useRouter();
  const { showSuccess } = useToast();
  const [showForm, setShowForm] = useState(false);

  return (
    <div className="min-h-screen bg-psipro-background">
      {/* Hero Section */}
      <section className="relative py-20 md:py-32 px-4">
        <div className="max-w-4xl mx-auto text-center">
          <h1 className="text-4xl md:text-5xl font-bold text-psipro-text mb-6 tracking-tight leading-tight">
            PsiPro — um sistema criado para facilitar a rotina do psicólogo
          </h1>
          <p className="text-xl md:text-2xl text-psipro-text-secondary mb-12 leading-relaxed max-w-3xl mx-auto">
            Estamos abrindo acesso beta para profissionais que desejam organizar agenda, pacientes e financeiro com mais clareza e menos ruído.
          </p>

          {/* CTA Principal */}
          <button
            onClick={() => setShowForm(true)}
            className="px-8 py-4 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium text-lg shadow-sm"
          >
            Solicitar acesso ao beta
          </button>
        </div>
      </section>

      {/* O que é o beta */}
      <section className="py-16 px-4 bg-psipro-surface-elevated">
        <div className="max-w-3xl mx-auto">
          <h2 className="text-2xl md:text-3xl font-semibold text-psipro-text mb-6 text-center">
            O que é o beta
          </h2>
          <div className="space-y-4 text-psipro-text-secondary leading-relaxed">
            <p>
              O PsiPro está em evolução constante. Estamos construindo um sistema que realmente ajuda psicólogos a organizar sua rotina, sem complicar.
            </p>
            <p>
              Durante o beta, o acesso é <strong className="text-psipro-text">gratuito</strong>. Nosso objetivo é aprender com você: o que funciona, o que precisa melhorar, o que realmente faz diferença no seu dia a dia.
            </p>
            <p>
              Seu feedback é bem-vindo e essencial para que o PsiPro se torne uma ferramenta cada vez mais útil para profissionais como você.
            </p>
          </div>
        </div>
      </section>

      {/* Valores do PsiPro */}
      <section className="py-16 px-4 bg-psipro-background">
        <div className="max-w-4xl mx-auto">
          <h2 className="text-2xl md:text-3xl font-semibold text-psipro-text mb-8 text-center">
            O que o PsiPro oferece
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[
              {
                title: "Clareza",
                description: "Uma visão simples e organizada do seu consultório, sem excesso de informações.",
              },
              {
                title: "Organização",
                description: "Agenda, pacientes e financeiro em um só lugar, sincronizado entre app e web.",
              },
              {
                title: "Apoio à rotina",
                description: "Menos esforço operacional, mais presença clínica. O PsiPro observa junto com você.",
              },
            ].map((value, index) => (
              <div
                key={index}
                className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6"
              >
                <h3 className="text-xl font-semibold text-psipro-text mb-3">{value.title}</h3>
                <p className="text-sm text-psipro-text-secondary leading-relaxed">{value.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Final */}
      <section className="py-20 px-4 bg-psipro-surface-elevated">
        <div className="max-w-3xl mx-auto text-center">
          <h2 className="text-2xl md:text-3xl font-semibold text-psipro-text mb-6">
            Um sistema que respeita seu tempo
          </h2>
          <p className="text-lg text-psipro-text-secondary mb-8 leading-relaxed">
            O PsiPro foi pensado para se integrar à sua rotina, não para complicá-la. Se você busca organização e clareza, este é o lugar certo.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button
              onClick={() => setShowForm(true)}
              className="px-8 py-4 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium text-lg shadow-sm"
            >
              Solicitar acesso ao beta
            </button>
            <Link
              href="/"
              className="px-8 py-4 bg-psipro-surface border border-psipro-border text-psipro-text rounded-lg hover:bg-psipro-surface-elevated transition-colors font-medium text-lg"
            >
              Voltar à página inicial
            </Link>
          </div>
        </div>
      </section>

      {/* Formulário de Solicitação */}
      {showForm && (
        <BetaAccessForm
          isOpen={showForm}
          onClose={() => setShowForm(false)}
          onSuccess={() => {
            setShowForm(false);
            showSuccess("Recebemos seu pedido. Em breve entraremos em contato com mais informações.");
          }}
        />
      )}
    </div>
  );
}

