"use client";

import BetaAccessForm from "./BetaAccessForm";

/**
 * Modal de Solicitação de Acesso Beta (compatibilidade)
 * 
 * Mantido para compatibilidade com código existente.
 * Agora usa o componente BetaAccessForm unificado.
 */
interface BetaAccessModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function BetaAccessModal({ isOpen, onClose }: BetaAccessModalProps) {
  return (
    <BetaAccessForm
      isOpen={isOpen}
      onClose={onClose}
      onSuccess={onClose}
    />
  );
}


