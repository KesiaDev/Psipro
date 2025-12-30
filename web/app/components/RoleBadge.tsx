"use client";

interface RoleBadgeProps {
  role: string;
  className?: string;
}

const getRoleLabel = (role: string) => {
  const labels: Record<string, string> = {
    owner: "Proprietário",
    admin: "Administrador",
    psychologist: "Psicólogo",
    assistant: "Assistente",
  };
  return labels[role] || role;
};

const getRoleBadgeColor = (role: string) => {
  const colors: Record<string, string> = {
    owner: "bg-psipro-primary/20 text-psipro-primary border-psipro-primary/30",
    admin: "bg-psipro-warning/20 text-psipro-warning border-psipro-warning/30",
    psychologist: "bg-psipro-success/20 text-psipro-success border-psipro-success/30",
    assistant: "bg-psipro-text-secondary/20 text-psipro-text-secondary border-psipro-text-secondary/30",
  };
  return colors[role] || "bg-psipro-surface text-psipro-text-secondary border-psipro-border";
};

export default function RoleBadge({ role, className = "" }: RoleBadgeProps) {
  return (
    <span
      className={`text-xs px-2 py-1 rounded border ${getRoleBadgeColor(role)} ${className}`}
    >
      {getRoleLabel(role)}
    </span>
  );
}



