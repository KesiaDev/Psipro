"use client";

interface InsightCardProps {
  message: string;
  type?: "info" | "warning" | "success" | "tip";
}

export default function InsightCard({ message, type = "info" }: InsightCardProps) {
  const iconMap = {
    info: "💡",
    warning: "⚠️",
    success: "✅",
    tip: "💬",
  };

  const bgColorMap = {
    info: "bg-psipro-primary/5 border-psipro-primary/20",
    warning: "bg-psipro-warning/5 border-psipro-warning/20",
    success: "bg-psipro-success/5 border-psipro-success/20",
    tip: "bg-psipro-surface border-psipro-border",
  };

  return (
    <div
      className={`rounded-lg border p-4 ${bgColorMap[type]} transition-all hover:shadow-sm`}
    >
      <div className="flex items-start gap-3">
        <span className="text-xl flex-shrink-0">{iconMap[type]}</span>
        <p className="text-sm text-psipro-text leading-relaxed flex-1">
          {message}
        </p>
      </div>
    </div>
  );
}



