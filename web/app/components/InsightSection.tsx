"use client";

import { useState } from "react";
import InsightCard, { Insight } from "./InsightCard";

interface InsightSectionProps {
  insights: Insight[];
  title?: string;
  maxItems?: number;
  showDisclaimer?: boolean;
}

export default function InsightSection({
  insights,
  title = "Insights do PsiPro",
  maxItems = 3,
  showDisclaimer = true,
}: InsightSectionProps) {
  const [dismissedIds, setDismissedIds] = useState<string[]>([]);

  const handleDismiss = (id: string) => {
    setDismissedIds((prev) => [...prev, id]);
  };

  const visibleInsights = insights
    .filter((insight) => !dismissedIds.includes(insight.id))
    .slice(0, maxItems);

  if (visibleInsights.length === 0) return null;

  return (
    <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
      <div className="p-6 border-b border-psipro-divider">
        <div className="flex items-center gap-2 mb-1">
          <span className="text-xl">🧠</span>
          <h2 className="text-lg font-semibold text-psipro-text">{title}</h2>
        </div>
        {showDisclaimer && (
          <p className="text-xs text-psipro-text-muted mt-2">
            Os insights do PsiPro são sugestões baseadas em padrões de uso e não substituem o
            julgamento profissional.
          </p>
        )}
      </div>
      <div className="p-6 space-y-3">
        {visibleInsights.map((insight) => (
          <InsightCard key={insight.id} insight={insight} onDismiss={handleDismiss} />
        ))}
      </div>
    </div>
  );
}




