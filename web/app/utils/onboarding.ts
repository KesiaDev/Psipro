/**
 * Utilitários para gerenciar o estado do onboarding
 * 
 * O onboarding é exibido apenas no primeiro acesso do usuário.
 * O estado é persistido no localStorage.
 */

const ONBOARDING_KEY = 'psipro_onboarding_completed';

/**
 * Verifica se é o primeiro acesso do usuário
 * @returns true se o onboarding ainda não foi completado
 */
export function isFirstAccess(): boolean {
  if (typeof window === 'undefined') return false;
  
  const completed = localStorage.getItem(ONBOARDING_KEY);
  return completed !== 'true';
}

/**
 * Marca o onboarding como completado
 */
export function markOnboardingCompleted(): void {
  if (typeof window === 'undefined') return;
  
  localStorage.setItem(ONBOARDING_KEY, 'true');
}

/**
 * Reseta o onboarding (útil para testes)
 */
export function resetOnboarding(): void {
  if (typeof window === 'undefined') return;
  
  localStorage.removeItem(ONBOARDING_KEY);
}



