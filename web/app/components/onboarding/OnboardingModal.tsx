"use client";

import { useOnboarding } from "../../contexts/OnboardingContext";
import OnboardingStep from "./OnboardingStep";
import OnboardingFooter from "./OnboardingFooter";

export default function OnboardingModal() {
  const {
    isOpen,
    currentStep,
    totalSteps,
    nextStep,
    previousStep,
    skipOnboarding,
    completeOnboarding,
    closeOnboarding,
  } = useOnboarding();

  if (!isOpen) return null;

  const handleNext = () => {
    if (currentStep === totalSteps) {
      completeOnboarding();
    } else {
      nextStep();
    }
  };

  return (
    <>
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        onClick={closeOnboarding}
      >
        {/* Modal */}
        <div
          className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto animate-in fade-in zoom-in-95 duration-300"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="p-8">
            {/* Conteúdo por etapa */}
            {currentStep === 1 && (
              <OnboardingStep
                title="Bem-vindo ao PsiPro"
                description="O PsiPro organiza sua agenda, pacientes e financeiro de forma simples — para você focar no que importa: o atendimento."
              >
                <div className="text-center py-8">
                  <div className="text-6xl mb-4">👋</div>
                  <p className="text-psipro-text-secondary text-lg">
                    Vamos começar?
                  </p>
                </div>
              </OnboardingStep>
            )}

            {currentStep === 2 && (
              <OnboardingStep title="Como o PsiPro funciona">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {/* Card 1 - App */}
                  <div className="bg-psipro-surface rounded-lg border border-psipro-border p-6 text-center">
                    <div className="text-4xl mb-3">📱</div>
                    <h3 className="font-semibold text-psipro-text mb-2">App</h3>
                    <p className="text-sm text-psipro-text-secondary">
                      Ações do dia a dia: agenda, atendimento, confirmação de
                      sessão
                    </p>
                  </div>

                  {/* Card 2 - Web */}
                  <div className="bg-psipro-surface rounded-lg border border-psipro-border p-6 text-center">
                    <div className="text-4xl mb-3">💻</div>
                    <h3 className="font-semibold text-psipro-text mb-2">Web</h3>
                    <p className="text-sm text-psipro-text-secondary">
                      Visão geral, financeiro, gestão da clínica, insights
                      inteligentes
                    </p>
                  </div>

                  {/* Card 3 - Sincronizado */}
                  <div className="bg-psipro-surface rounded-lg border border-psipro-border p-6 text-center">
                    <div className="text-4xl mb-3">🔄</div>
                    <h3 className="font-semibold text-psipro-text mb-2">
                      Tudo sincronizado
                    </h3>
                    <p className="text-sm text-psipro-text-secondary">
                      O que você faz em um, aparece no outro automaticamente
                    </p>
                  </div>
                </div>
              </OnboardingStep>
            )}

            {currentStep === 3 && (
              <OnboardingStep
                title="O que fazer primeiro"
                description="Você pode fazer isso aos poucos. O PsiPro se adapta à sua rotina."
              >
                <div className="space-y-4 max-w-md mx-auto">
                  <div className="flex items-start gap-3 p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                    <span className="text-psipro-primary text-xl">✓</span>
                    <div>
                      <p className="font-medium text-psipro-text">
                        Criar ou confirmar sua clínica
                      </p>
                      <p className="text-sm text-psipro-text-secondary mt-1">
                        Organize sua prática profissional
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3 p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                    <span className="text-psipro-primary text-xl">✓</span>
                    <div>
                      <p className="font-medium text-psipro-text">
                        Cadastrar seus primeiros pacientes
                      </p>
                      <p className="text-sm text-psipro-text-secondary mt-1">
                        Importe ou cadastre manualmente
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3 p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                    <span className="text-psipro-primary text-xl">✓</span>
                    <div>
                      <p className="font-medium text-psipro-text">
                        Organizar agenda
                      </p>
                      <p className="text-sm text-psipro-text-secondary mt-1">
                        Use o app para agendar e confirmar
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start gap-3 p-4 bg-psipro-surface rounded-lg border border-psipro-border">
                    <span className="text-psipro-primary text-xl">✓</span>
                    <div>
                      <p className="font-medium text-psipro-text">
                        Acompanhar financeiro automaticamente
                      </p>
                      <p className="text-sm text-psipro-text-secondary mt-1">
                        Tudo é calculado a partir das sessões
                      </p>
                    </div>
                  </div>
                </div>
              </OnboardingStep>
            )}

            {currentStep === 4 && (
              <OnboardingStep
                title="Dashboard explicado"
                description="Aqui você acompanha tudo o que precisa, sem procurar em menus."
              >
                <div className="space-y-4">
                  <div className="p-4 bg-psipro-primary/5 border border-psipro-primary/20 rounded-lg">
                    <div className="flex items-start gap-3">
                      <span className="text-2xl">📊</span>
                      <div>
                        <p className="font-semibold text-psipro-text mb-1">
                          Cards de métricas
                        </p>
                        <p className="text-sm text-psipro-text-secondary">
                          Veja pacientes, sessões e receita em um só lugar
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="p-4 bg-psipro-success/5 border border-psipro-success/20 rounded-lg">
                    <div className="flex items-start gap-3">
                      <span className="text-2xl">📅</span>
                      <div>
                        <p className="font-semibold text-psipro-text mb-1">
                          Bloco de agenda
                        </p>
                        <p className="text-sm text-psipro-text-secondary">
                          Resumo da semana e dias mais movimentados
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="p-4 bg-psipro-warning/5 border border-psipro-warning/20 rounded-lg">
                    <div className="flex items-start gap-3">
                      <span className="text-2xl">💰</span>
                      <div>
                        <p className="font-semibold text-psipro-text mb-1">
                          Bloco financeiro
                        </p>
                        <p className="text-sm text-psipro-text-secondary">
                          Receita, valores a receber e médias por sessão
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="p-4 bg-psipro-surface border border-psipro-border rounded-lg">
                    <div className="flex items-start gap-3">
                      <span className="text-2xl">💡</span>
                      <div>
                        <p className="font-semibold text-psipro-text mb-1">
                          Bloco de insights
                        </p>
                        <p className="text-sm text-psipro-text-secondary">
                          Observações automáticas sobre sua prática
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </OnboardingStep>
            )}

            {currentStep === 5 && (
              <OnboardingStep
                title="Pronto!"
                description="O PsiPro está preparado para crescer com você."
              >
                <div className="text-center py-8">
                  <div className="text-6xl mb-4">🎉</div>
                  <p className="text-psipro-text-secondary text-lg mb-6">
                    Você já sabe tudo o que precisa para começar.
                  </p>
                  <p className="text-psipro-text-secondary">
                    Explore o dashboard e descubra como o PsiPro pode ajudar
                    sua prática clínica.
                  </p>
                </div>
              </OnboardingStep>
            )}

            {/* Footer com navegação */}
            <OnboardingFooter
              currentStep={currentStep}
              totalSteps={totalSteps}
              onNext={handleNext}
              onPrevious={previousStep}
              onSkip={skipOnboarding}
              nextLabel={currentStep === 1 ? "Começar" : "Próximo"}
              showPrevious={currentStep > 1}
              isLastStep={currentStep === totalSteps}
            />
          </div>
        </div>
      </div>
    </>
  );
}



