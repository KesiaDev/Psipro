export default function FinanceiroPacienteTab({ patientId }: { patientId: string }) {
  // Dados mockados
  const financialCards = [
    {
      title: "Total faturado",
      value: "R$ 1.800,00",
      subtitle: "12 sessões",
    },
    {
      title: "Total recebido",
      value: "R$ 1.500,00",
      subtitle: "10 pagamentos",
    },
    {
      title: "Total em aberto",
      value: "R$ 300,00",
      subtitle: "2 pendências",
    },
  ];

  const charges = [
    {
      id: "1",
      date: "12/03/2025",
      value: "R$ 150,00",
      status: "pago",
      session: "Sessão 12/03",
    },
    {
      id: "2",
      date: "05/03/2025",
      value: "R$ 150,00",
      status: "pago",
      session: "Sessão 05/03",
    },
    {
      id: "3",
      date: "26/02/2025",
      value: "R$ 150,00",
      status: "pendente",
      session: "Sessão 26/02",
    },
    {
      id: "4",
      date: "19/02/2025",
      value: "R$ 150,00",
      status: "pendente",
      session: "Sessão 19/02",
    },
    {
      id: "5",
      date: "12/02/2025",
      value: "R$ 150,00",
      status: "pago",
      session: "Sessão 12/02",
    },
  ];

  return (
    <div className="space-y-6">
      {/* Cards de resumo */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {financialCards.map((card, index) => (
          <div
            key={index}
            className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 hover:shadow-md transition-all duration-200"
          >
            <p className="text-sm text-psipro-text-secondary mb-2 font-medium">{card.title}</p>
            <p className="text-3xl font-bold text-psipro-text mb-1">{card.value}</p>
            <p className="text-xs text-psipro-text-muted">{card.subtitle}</p>
          </div>
        ))}
      </div>

      {/* Lista de cobranças */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Cobranças</h2>
        </div>
        <div className="divide-y divide-psipro-divider">
          {charges.map((charge) => (
            <div key={charge.id} className="p-6 hover:bg-psipro-surface transition-colors">
              <div className="flex items-center justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-1">
                    <span className="text-sm font-medium text-psipro-text">{charge.date}</span>
                    <span className="text-sm text-psipro-text-secondary">{charge.session}</span>
                  </div>
                  <span
                    className={`inline-flex px-2.5 py-1 text-xs font-medium rounded-full ${
                      charge.status === "pago"
                        ? "bg-psipro-success/20 text-psipro-success"
                        : "bg-psipro-warning/20 text-psipro-warning"
                    }`}
                  >
                    {charge.status === "pago" ? "Pago" : "Pendente"}
                  </span>
                </div>
                <div className="text-right">
                  <p className="text-lg font-semibold text-psipro-text">{charge.value}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Texto orientativo */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
        <div className="flex items-center gap-3">
          <div className="text-2xl opacity-60">📱</div>
          <div className="flex-1">
            <p className="text-sm text-psipro-text-secondary">
              <strong>O registro de pagamentos é feito no app PsiPro.</strong> A web é usada para
              acompanhamento e análise financeira consolidada.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

