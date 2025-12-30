"use client";

interface OnboardingStepProps {
  children: React.ReactNode;
  title: string;
  description?: string;
}

export default function OnboardingStep({
  children,
  title,
  description,
}: OnboardingStepProps) {
  return (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-psipro-text mb-3">{title}</h2>
        {description && (
          <p className="text-psipro-text-secondary text-base leading-relaxed max-w-md mx-auto">
            {description}
          </p>
        )}
      </div>
      <div className="mt-8">{children}</div>
    </div>
  );
}



