import InsightSection from "@/app/components/InsightSection";
import { Insight } from "@/app/components/InsightCard";

export default function VisaoGeralTab({ patientId }: { patientId: string }) {
  // Dados mockados - Insights do paciente
  const patientInsights: Insight[] = [
    {
      id: `patient-${patientId}-1`,
      type: "clinico",
      message: "Este paciente apresenta faltas frequentes nas últimas semanas.",
    },
    {
      id: `patient-${patientId}-2`,
      type: "financeiro",
      message: "Situação financeira em dia. Sem pendências.",
    },
  ];
  // Dados mockados
  const clinicalSummary = {
    lastSession: "12/03/2025",
    frequency: "Semanal",
    observations:
      "Paciente demonstra evolução positiva. Aplicando técnicas de relaxamento com consistência.",
  };

  const financialSummary = {
    sessionValue: "R$ 150,00",
    totalReceived: "R$ 1.500,00",
    totalPending: "R$ 300,00",
    status: "Em dia",
  };

  const timeline = [
    {
      date: "12/03/2025",
      type: "session",
      status: "realizada",
      note: "Sessão focada em técnicas de relaxamento...",
    },
    {
      date: "05/03/2025",
      type: "session",
      status: "realizada",
      note: "Discussão sobre padrões de ansiedade...",
    },
    {
      date: "26/02/2025",
      type: "session",
      status: "realizada",
      note: "Avaliação de progresso e estabelecimento de novas metas...",
    },
  ];

  return (
    <div className="space-y-6">
      {/* Card Resumo Clínico */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Resumo Clínico</h2>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Última sessão</p>
              <p className="text-base font-semibold text-psipro-text">{clinicalSummary.lastSession}</p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Frequência</p>
              <p className="text-base font-semibold text-psipro-text">{clinicalSummary.frequency}</p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Status</p>
              <span className="inline-flex px-2.5 py-1 text-xs font-medium rounded-full bg-psipro-success/20 text-psipro-success">
                Ativo
              </span>
            </div>
          </div>
          <div>
            <p className="text-sm text-psipro-text-secondary mb-2">Observações gerais</p>
            <p className="text-sm text-psipro-text leading-relaxed">
              {clinicalSummary.observations}
            </p>
          </div>
        </div>
      </div>

      {/* Card Resumo Financeiro */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Resumo Financeiro</h2>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Valor da sessão</p>
              <p className="text-lg font-semibold text-psipro-text">
                {financialSummary.sessionValue}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Total recebido</p>
              <p className="text-lg font-semibold text-psipro-success">
                {financialSummary.totalReceived}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Total em aberto</p>
              <p className="text-lg font-semibold text-psipro-warning">
                {financialSummary.totalPending}
              </p>
            </div>
            <div>
              <p className="text-sm text-psipro-text-secondary mb-1">Status financeiro</p>
              <span className="inline-flex px-2.5 py-1 text-xs font-medium rounded-full bg-psipro-success/20 text-psipro-success">
                {financialSummary.status}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Card Linha do Tempo */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Linha do Tempo</h2>
          <p className="text-xs text-psipro-text-muted mt-1">Últimas sessões e anotações</p>
        </div>
        <div className="p-6">
          <div className="space-y-4">
            {timeline.map((item, index) => (
              <div key={index} className="flex gap-4">
                <div className="flex-shrink-0">
                  <div className="w-2 h-2 rounded-full bg-psipro-primary mt-2"></div>
                  {index < timeline.length - 1 && (
                    <div className="w-0.5 h-full bg-psipro-border ml-0.5 mt-1"></div>
                  )}
                </div>
                <div className="flex-1 pb-4">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-sm font-medium text-psipro-text">{item.date}</span>
                    <span
                      className={`inline-flex px-2 py-0.5 text-xs font-medium rounded-full ${
                        item.status === "realizada"
                          ? "bg-psipro-success/20 text-psipro-success"
                          : "bg-psipro-text-muted/20 text-psipro-text-muted"
                      }`}
                    >
                      {item.status === "realizada" ? "Realizada" : item.status}
                    </span>
                  </div>
                  {item.note && (
                    <p className="text-sm text-psipro-text-secondary leading-relaxed">
                      {item.note}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Insights do Paciente */}
      <div className="mt-6">
        <InsightSection insights={patientInsights} maxItems={2} title="Insights do Paciente" />
      </div>
    </div>
  );
}

