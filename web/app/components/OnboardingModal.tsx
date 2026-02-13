"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

type Step = 1 | 2 | 3 | 4;

export default function OnboardingModal() {
  const [isOpen, setIsOpen] = useState(false);
  const [currentStep, setCurrentStep] = useState<Step>(1);
  const router = useRouter();

  useEffect(() => {
    // Verificar se o onboarding já foi concluído
    const onboardingCompleted = localStorage.getItem("psipro_onboarding_completed");
    if (onboardingCompleted !== "true") {
      setIsOpen(true);
    }
  }, []);

  const handleNext = () => {
    if (currentStep < 4) {
      setCurrentStep((prev) => (prev + 1) as Step);
    }
  };

  const handleComplete = () => {
    localStorage.setItem("psipro_onboarding_completed", "true");
    setIsOpen(false);
    router.push("/dashboard");
  };

  const handleSkip = () => {
    localStorage.setItem("psipro_onboarding_completed", "true");
    setIsOpen(false);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-psipro-overlay p-4">
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header com botão de pular */}
        <div className="p-6 border-b border-psipro-divider flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="flex gap-1">
              {[1, 2, 3, 4].map((step) => (
                <div
                  key={step}
                  className={`h-1.5 w-8 rounded-full transition-all ${
                    step <= currentStep
                      ? "bg-psipro-primary"
                      : "bg-psipro-border"
                  }`}
                />
              ))}
            </div>
            <span className="text-xs text-psipro-text-muted ml-2">
              {currentStep} de 4
            </span>
          </div>
          <button
            onClick={handleSkip}
            className="text-sm text-psipro-text-muted hover:text-psipro-text transition-colors"
          >
            Pular
          </button>
        </div>

        {/* Conteúdo do passo */}
        <div className="p-8">
          {currentStep === 1 && (
            <div className="text-center">
              <div className="text-6xl mb-6 opacity-80">👋</div>
              <h2 className="text-2xl font-bold text-psipro-text mb-4">
                Bem-vindo ao PsiPro
              </h2>
              <p className="text-base text-psipro-text-secondary mb-6 leading-relaxed">
                O PsiPro foi criado para facilitar sua rotina clínica e ajudar você a ter mais
                clareza sobre sua prática.
              </p>
              <div className="bg-psipro-surface rounded-lg p-6 border border-psipro-border text-left mb-6">
                <ul className="space-y-3 text-sm text-psipro-text-secondary">
                  <li className="flex items-start gap-3">
                    <span className="text-psipro-primary mt-0.5 text-lg">📱</span>
                    <span>
                      <strong className="text-psipro-text">App:</strong> para atendimentos e
                      sessões
                    </span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-psipro-primary mt-0.5 text-lg">💻</span>
                    <span>
                      <strong className="text-psipro-text">Web:</strong> para gestão, organização
                      e análise
                    </span>
                  </li>
                </ul>
              </div>
              <button
                onClick={handleNext}
                className="px-8 py-3 text-base font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all shadow-sm"
              >
                Entendi
              </button>
            </div>
          )}

          {currentStep === 2 && (
            <div className="text-center">
              <div className="text-6xl mb-6 opacity-80">📚</div>
              <h2 className="text-2xl font-bold text-psipro-text mb-4">
                Como usar o PsiPro
              </h2>
              <p className="text-base text-psipro-text-secondary mb-6 leading-relaxed">
                No dia a dia, você usa o app para atender seus pacientes. Na web, você acompanha
                tudo com mais visão e conforto.
              </p>
              <div className="bg-psipro-surface rounded-lg p-6 border border-psipro-border text-left mb-6">
                <ul className="space-y-3 text-sm text-psipro-text-secondary">
                  <li className="flex items-start gap-3">
                    <span className="text-psipro-primary mt-0.5">✓</span>
                    <span>Agende e realize sessões no app</span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-psipro-primary mt-0.5">✓</span>
                    <span>Registre pagamentos no app</span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-psipro-primary mt-0.5">✓</span>
                    <span>Acompanhe agenda e financeiro na web</span>
                  </li>
                </ul>
              </div>
              <button
                onClick={handleNext}
                className="px-8 py-3 text-base font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all shadow-sm"
              >
                Continuar
              </button>
            </div>
          )}

          {currentStep === 3 && (
            <div className="text-center">
              <div className="text-6xl mb-6 opacity-80">🚀</div>
              <h2 className="text-2xl font-bold text-psipro-text mb-4">
                Por onde começar?
              </h2>
              <p className="text-base text-psipro-text-secondary mb-6 leading-relaxed">
                Para aproveitar melhor o PsiPro, sugerimos começar por aqui:
              </p>
              <div className="bg-psipro-surface rounded-lg p-6 border border-psipro-border text-left mb-6">
                <ul className="space-y-4 text-sm text-psipro-text-secondary">
                  <li className="flex items-start gap-3">
                    <span className="text-psipro-primary mt-0.5 text-lg">📊</span>
                    <span>
                      <strong className="text-psipro-text">Importe seus pacientes</strong>
                      <br />
                      <span className="text-xs text-psipro-text-muted">
                        Use a página de Pacientes para organizar sua base
                      </span>
                    </span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-psipro-primary mt-0.5 text-lg">📅</span>
                    <span>
                      <strong className="text-psipro-text">Configure sua agenda</strong>
                      <br />
                      <span className="text-xs text-psipro-text-muted">
                        Visualize e planeje seus atendimentos
                      </span>
                    </span>
                  </li>
                  <li className="flex items-start gap-3">
                    <span className="text-psipro-primary mt-0.5 text-lg">📱</span>
                    <span>
                      <strong className="text-psipro-text">Realize a primeira sessão pelo app</strong>
                      <br />
                      <span className="text-xs text-psipro-text-muted">
                        Use o app Android para atendimentos do dia a dia
                      </span>
                    </span>
                  </li>
                </ul>
              </div>
              <button
                onClick={handleNext}
                className="px-8 py-3 text-base font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all shadow-sm"
              >
                Vamos lá
              </button>
            </div>
          )}

          {currentStep === 4 && (
            <div className="text-center">
              <div className="text-6xl mb-6 opacity-80">🎉</div>
              <h2 className="text-2xl font-bold text-psipro-text mb-4">Tudo pronto</h2>
              <p className="text-base text-psipro-text-secondary mb-8 leading-relaxed">
                Você já pode usar o PsiPro. Sempre que precisar, estaremos aqui para ajudar.
              </p>
              <button
                onClick={handleComplete}
                className="px-8 py-3 text-base font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all shadow-sm"
              >
                Ir para o Dashboard
              </button>
            </div>
          )}
        </div>

        {/* Navegação entre passos */}
        {currentStep > 1 && (
          <div className="p-6 border-t border-psipro-divider flex justify-between">
            <button
              onClick={() => setCurrentStep((prev) => (prev - 1) as Step)}
              className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
            >
              Voltar
            </button>
            {currentStep < 4 && (
              <button
                onClick={handleNext}
                className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all"
              >
                Próximo
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}




