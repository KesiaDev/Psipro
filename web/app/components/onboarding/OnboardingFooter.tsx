"use client";

interface OnboardingFooterProps {
  currentStep: number;
  totalSteps: number;
  onNext: () => void;
  onPrevious: () => void;
  onSkip: () => void;
  nextLabel?: string;
  showPrevious?: boolean;
  isLastStep?: boolean;
}

export default function OnboardingFooter({
  currentStep,
  totalSteps,
  onNext,
  onPrevious,
  onSkip,
  nextLabel,
  showPrevious = true,
  isLastStep = false,
}: OnboardingFooterProps) {
  return (
    <div className="mt-8 pt-6 border-t border-psipro-border">
      <div className="flex items-center justify-between">
        {/* Indicador de progresso */}
        <div className="flex items-center gap-2">
          {Array.from({ length: totalSteps }).map((_, index) => (
            <div
              key={index}
              className={`h-2 w-2 rounded-full transition-all ${
                index + 1 <= currentStep
                  ? "bg-psipro-primary w-8"
                  : "bg-psipro-border"
              }`}
            />
          ))}
        </div>

        {/* Botões */}
        <div className="flex items-center gap-3">
          {showPrevious && currentStep > 1 && (
            <button
              onClick={onPrevious}
              className="px-4 py-2 text-sm text-psipro-text-secondary hover:text-psipro-text transition-colors"
            >
              Anterior
            </button>
          )}
          <button
            onClick={onSkip}
            className="px-4 py-2 text-sm text-psipro-text-secondary hover:text-psipro-text transition-colors"
          >
            Pular
          </button>
          <button
            onClick={onNext}
            className="px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors font-medium"
          >
            {isLastStep ? "Ir para o Dashboard" : nextLabel || "Próximo"}
          </button>
        </div>
      </div>
    </div>
  );
}


