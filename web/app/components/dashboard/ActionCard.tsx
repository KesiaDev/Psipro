"use client";

import Link from "next/link";

interface Action {
  label: string;
  href: string;
  variant?: "primary" | "secondary";
}

interface ActionCardProps {
  title: string;
  description?: string;
  actions: Action[];
}

export default function ActionCard({
  title,
  description,
  actions,
}: ActionCardProps) {
  return (
    <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
      <h3 className="text-lg font-semibold text-psipro-text mb-2">{title}</h3>
      {description && (
        <p className="text-sm text-psipro-text-secondary mb-4">
          {description}
        </p>
      )}
      <div className="flex flex-wrap gap-3">
        {actions.map((action, index) => (
          <Link
            key={index}
            href={action.href}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              action.variant === "primary"
                ? "bg-psipro-primary text-white hover:bg-psipro-primary-dark"
                : "bg-psipro-surface border border-psipro-border text-psipro-text hover:bg-psipro-surface-elevated"
            }`}
          >
            {action.label}
          </Link>
        ))}
      </div>
    </div>
  );
}


