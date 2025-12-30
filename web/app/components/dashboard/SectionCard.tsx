"use client";

interface SectionCardProps {
  title: string;
  children: React.ReactNode;
  emptyState?: {
    icon: string;
    title: string;
    description: string;
    hint?: string;
  };
  isEmpty?: boolean;
  className?: string;
}

export default function SectionCard({
  title,
  children,
  emptyState,
  isEmpty = false,
  className = "",
}: SectionCardProps) {
  return (
    <div
      className={`bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm ${className}`}
    >
      <div className="p-6 border-b border-psipro-border">
        <h2 className="text-lg font-semibold text-psipro-text">{title}</h2>
      </div>
      <div className="p-6">
        {isEmpty && emptyState ? (
          <div className="text-center py-12">
            <div className="text-5xl mb-4 opacity-40">{emptyState.icon}</div>
            <p className="text-psipro-text font-medium mb-2">
              {emptyState.title}
            </p>
            <p className="text-psipro-text-secondary text-sm mb-2 max-w-md mx-auto">
              {emptyState.description}
            </p>
            {emptyState.hint && (
              <p className="text-psipro-text-secondary/70 text-xs mt-3 italic">
                {emptyState.hint}
              </p>
            )}
          </div>
        ) : (
          children
        )}
      </div>
    </div>
  );
}


