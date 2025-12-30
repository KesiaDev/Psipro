"use client";

import { useState } from "react";

export interface Insight {
  id: string;
  type: "clinico" | "financeiro" | "agenda";
  message: string;
  dismissible?: boolean;
}

interface InsightCardProps {
  insight: Insight;
  onDismiss?: (id: string) => void;
}

export default function InsightCard({ insight, onDismiss }: InsightCardProps) {
  const [isDismissed, setIsDismissed] = useState(false);

  const handleDismiss = () => {
    setIsDismissed(true);
    if (onDismiss) {
      onDismiss(insight.id);
    }
  };

  if (isDismissed) return null;

  const icons = {
    clinico: "🧠",
    financeiro: "💰",
    agenda: "📅",
  };

  const bgColors = {
    clinico: "bg-psipro-primary/5 border-psipro-primary/20",
    financeiro: "bg-psipro-success/5 border-psipro-success/20",
    agenda: "bg-psipro-warning/5 border-psipro-warning/20",
  };

  return (
    <div
      className={`rounded-lg border p-4 transition-all ${bgColors[insight.type] || "bg-psipro-surface-elevated border-psipro-border"}`}
    >
      <div className="flex items-start gap-3">
        <span className="text-xl flex-shrink-0">{icons[insight.type]}</span>
        <div className="flex-1 min-w-0">
          <p className="text-sm text-psipro-text leading-relaxed">{insight.message}</p>
        </div>
        {insight.dismissible !== false && (
          <button
            onClick={handleDismiss}
            className="flex-shrink-0 text-xs text-psipro-text-muted hover:text-psipro-text transition-colors px-2 py-1"
            title="Ocultar insight"
          >
            ✕
          </button>
        )}
      </div>
    </div>
  );
}




