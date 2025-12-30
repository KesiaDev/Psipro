"use client";

import React, { createContext, useContext, useState, useCallback } from "react";
import { markOnboardingCompleted } from "../utils/onboarding";

type OnboardingStep = 1 | 2 | 3 | 4 | 5;

interface OnboardingContextType {
  isOpen: boolean;
  currentStep: OnboardingStep;
  totalSteps: number;
  openOnboarding: () => void;
  closeOnboarding: () => void;
  nextStep: () => void;
  previousStep: () => void;
  skipOnboarding: () => void;
  completeOnboarding: () => void;
  goToStep: (step: OnboardingStep) => void;
}

const OnboardingContext = createContext<OnboardingContextType | undefined>(
  undefined
);

export function OnboardingProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [currentStep, setCurrentStep] = useState<OnboardingStep>(1);
  const totalSteps = 5;

  const openOnboarding = useCallback(() => {
    setIsOpen(true);
    setCurrentStep(1);
  }, []);

  const closeOnboarding = useCallback(() => {
    setIsOpen(false);
  }, []);

  const nextStep = useCallback(() => {
    setCurrentStep((prev) => {
      if (prev < totalSteps) {
        return (prev + 1) as OnboardingStep;
      }
      return prev;
    });
  }, [totalSteps]);

  const previousStep = useCallback(() => {
    setCurrentStep((prev) => {
      if (prev > 1) {
        return (prev - 1) as OnboardingStep;
      }
      return prev;
    });
  }, []);

  const skipOnboarding = useCallback(() => {
    markOnboardingCompleted();
    setIsOpen(false);
  }, []);

  const completeOnboarding = useCallback(() => {
    markOnboardingCompleted();
    setIsOpen(false);
    setCurrentStep(1);
  }, []);

  const goToStep = useCallback((step: OnboardingStep) => {
    setCurrentStep(step);
  }, []);

  return (
    <OnboardingContext.Provider
      value={{
        isOpen,
        currentStep,
        totalSteps,
        openOnboarding,
        closeOnboarding,
        nextStep,
        previousStep,
        skipOnboarding,
        completeOnboarding,
        goToStep,
      }}
    >
      {children}
    </OnboardingContext.Provider>
  );
}

export function useOnboarding() {
  const context = useContext(OnboardingContext);
  if (context === undefined) {
    throw new Error(
      "useOnboarding must be used within an OnboardingProvider"
    );
  }
  return context;
}


