export default function SessoesTab() {
  const sessions = [
    {
      id: 1,
      date: "15/03/2025",
      time: "14:00",
      status: "Agendada",
      value: "R$ 150,00",
      paid: false,
    },
    {
      id: 2,
      date: "12/03/2025",
      time: "14:00",
      status: "Realizada",
      value: "R$ 150,00",
      paid: true,
    },
    {
      id: 3,
      date: "05/03/2025",
      time: "14:00",
      status: "Realizada",
      value: "R$ 150,00",
      paid: true,
    },
    {
      id: 4,
      date: "26/02/2025",
      time: "14:00",
      status: "Falta",
      value: "R$ 150,00",
      paid: false,
    },
    {
      id: 5,
      date: "19/02/2025",
      time: "14:00",
      status: "Realizada",
      value: "R$ 150,00",
      paid: true,
    },
  ];

  const getStatusBadge = (status: string) => {
    const styles = {
      Realizada: "bg-psipro-success/20 text-psipro-success",
      Agendada: "bg-psipro-primary/20 text-psipro-primary",
      Falta: "bg-psipro-error/20 text-psipro-error",
      Cancelada: "bg-psipro-text-muted/20 text-psipro-text-muted",
    };
    return styles[status as keyof typeof styles] || styles.Cancelada;
  };

  return (
    <div>
      {sessions.length > 0 ? (
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border overflow-hidden shadow-sm">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-psipro-surface border-b border-psipro-border">
                <tr>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-psipro-text-secondary uppercase tracking-wider">
                    Data
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-psipro-text-secondary uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-psipro-text-secondary uppercase tracking-wider">
                    Valor
                  </th>
                  <th className="px-6 py-4 text-left text-xs font-semibold text-psipro-text-secondary uppercase tracking-wider">
                    Pago
                  </th>
                </tr>
              </thead>
              <tbody className="bg-psipro-surface-elevated divide-y divide-psipro-divider">
                {sessions.map((session) => (
                  <tr key={session.id} className="hover:bg-psipro-surface transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-psipro-text">
                        {session.date}
                      </div>
                      <div className="text-sm text-psipro-text-muted">{session.time}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`inline-flex px-3 py-1 text-xs font-medium rounded-full ${getStatusBadge(
                          session.status
                        )}`}
                      >
                        {session.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-psipro-text">
                      {session.value}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {session.paid ? (
                        <span className="text-psipro-success text-sm font-medium">✓ Sim</span>
                      ) : (
                        <span className="text-psipro-text-muted text-sm">Não</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-12 text-center">
          <div className="text-5xl mb-4 opacity-60">📅</div>
          <p className="text-psipro-text-secondary text-base mb-2 font-medium">
            Você ainda não registrou sessões para este paciente.
          </p>
          <p className="text-psipro-text-muted text-sm">
            As sessões agendadas e realizadas aparecerão aqui.
          </p>
        </div>
      )}
    </div>
  );
}

