export default function SummaryTab() {
  const stats = [
    { label: "Próxima sessão", value: "15/03/2025", icon: "📅" },
    { label: "Sessões realizadas", value: "12", icon: "✅" },
    { label: "Faltas", value: "2", icon: "❌" },
    { label: "Valor da sessão", value: "R$ 150,00", icon: "💵" },
    { label: "Total pago", value: "R$ 1.500,00", icon: "💰" },
    { label: "Em aberto", value: "R$ 300,00", icon: "⏳" },
  ];

  return (
    <div>
      {/* Cards de estatísticas */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5 mb-8">
        {stats.map((stat, index) => (
          <div
            key={index}
            className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 hover:shadow-md transition-all duration-200"
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <p className="text-sm text-psipro-text-secondary mb-2 font-medium">{stat.label}</p>
                <p className="text-2xl font-bold text-psipro-text">{stat.value}</p>
              </div>
              <span className="text-2xl opacity-80">{stat.icon}</span>
            </div>
          </div>
        ))}
      </div>

      {/* Última anotação */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-psipro-text">Última anotação</h3>
            <span className="text-sm text-psipro-text-muted">12/03/2025</span>
          </div>
        </div>
        <div className="p-6">
          <p className="text-psipro-text-secondary leading-relaxed text-base">
            Paciente relatou melhora significativa na qualidade do sono após
            implementação das técnicas de relaxamento discutidas na sessão anterior.
            Demonstrou maior consciência sobre os padrões de ansiedade e está
            aplicando as estratégias de forma consistente.
          </p>
        </div>
      </div>
    </div>
  );
}

