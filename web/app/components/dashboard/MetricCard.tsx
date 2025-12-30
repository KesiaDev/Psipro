"use client";

interface MetricCardProps {
  title: string;
  value: string | number;
  icon: string;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  subtitle?: string;
}

export default function MetricCard({
  title,
  value,
  icon,
  trend,
  subtitle,
}: MetricCardProps) {
  return (
    <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 hover:shadow-md transition-all duration-200">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm text-psipro-text-secondary mb-2 font-medium">
            {title}
          </p>
          <p className="text-3xl font-bold text-psipro-text mb-1">{value}</p>
          {subtitle && (
            <p className="text-xs text-psipro-text-secondary">{subtitle}</p>
          )}
          {trend && (
            <div className="flex items-center gap-1 mt-2">
              <span
                className={`text-xs font-medium ${
                  trend.isPositive
                    ? "text-psipro-success"
                    : "text-psipro-error"
                }`}
              >
                {trend.isPositive ? "↑" : "↓"} {Math.abs(trend.value)}%
              </span>
              <span className="text-xs text-psipro-text-secondary">
                vs mês anterior
              </span>
            </div>
          )}
        </div>
        <span className="text-3xl opacity-60">{icon}</span>
      </div>
    </div>
  );
}


