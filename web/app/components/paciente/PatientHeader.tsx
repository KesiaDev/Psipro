export default function PatientHeader({ patientId }: { patientId: string }) {
  // Dados mockados - serão substituídos por dados reais depois
  const patient = {
    name: "Maria Silva Santos",
    status: "Ativo",
    type: "Adulto",
    nextSession: "15/03/2025 às 14:00",
    totalSessions: 12,
    sessionsThisMonth: 3,
    financialStatus: "Em dia",
  };

  return (
    <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
      <div className="p-6">
        <div className="flex items-start gap-6">
          {/* Avatar */}
          <div className="flex-shrink-0">
            <div className="h-20 w-20 rounded-full bg-psipro-primary border-2 border-psipro-primary-dark flex items-center justify-center shadow-sm">
              <span className="text-2xl font-semibold text-psipro-text">
                {patient.name.charAt(0)}
              </span>
            </div>
          </div>

          {/* Informações principais */}
          <div className="flex-1 min-w-0">
            <div className="flex items-start justify-between gap-6">
              <div className="flex-1">
                <h1 className="text-2xl font-bold text-psipro-text mb-3 tracking-tight">
                  {patient.name}
                </h1>
                
                {/* Status e tipo */}
                <div className="flex items-center gap-4 text-sm mb-3">
                  <span
                    className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                      patient.status === "Ativo"
                        ? "bg-psipro-success/20 text-psipro-success"
                        : "bg-psipro-text-muted/20 text-psipro-text-muted"
                    }`}
                  >
                    <span
                      className={`h-2 w-2 rounded-full ${
                        patient.status === "Ativo"
                          ? "bg-psipro-success"
                          : "bg-psipro-text-muted"
                      }`}
                    ></span>
                    {patient.status}
                  </span>
                  <span className="text-psipro-text-secondary">{patient.type}</span>
                  {patient.nextSession && (
                    <span className="text-psipro-text-secondary">
                      Próxima: {patient.nextSession}
                    </span>
                  )}
                </div>

                {/* Indicadores rápidos */}
                <div className="flex items-center gap-6 text-sm">
                  <div className="flex items-center gap-2">
                    <span className="text-psipro-text-muted">Total de sessões:</span>
                    <span className="font-semibold text-psipro-text">{patient.totalSessions}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-psipro-text-muted">Sessões no mês:</span>
                    <span className="font-semibold text-psipro-text">
                      {patient.sessionsThisMonth}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-psipro-text-muted">Situação financeira:</span>
                    <span
                      className={`font-semibold ${
                        patient.financialStatus === "Em dia"
                          ? "text-psipro-success"
                          : "text-psipro-warning"
                      }`}
                    >
                      {patient.financialStatus}
                    </span>
                  </div>
                </div>
              </div>

              {/* Ações disponíveis */}
              <div className="flex items-center gap-2.5 flex-shrink-0 flex-wrap">
                <button className="px-4 py-2.5 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated hover:border-psipro-primary/30 transition-all shadow-sm">
                  Ver agenda
                </button>
                <button className="px-4 py-2.5 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated hover:border-psipro-primary/30 transition-all shadow-sm">
                  Ver financeiro
                </button>
                <div className="px-4 py-2.5 text-xs text-psipro-text-muted bg-psipro-surface border border-psipro-border rounded-lg">
                  Abrir no app
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
