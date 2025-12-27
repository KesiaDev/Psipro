"use client";

import { useState } from "react";

export default function FinanceiroTab() {
  const [isEditing, setIsEditing] = useState(false);

  const financialSettings = {
    valorSessao: "R$ 150,00",
    diaCobranca: "15",
    lembretes: true,
  };

  const summary = {
    totalPaid: "R$ 1.500,00",
    totalPending: "R$ 300,00",
    totalAtrasado: "R$ 150,00",
    totalSessions: 12,
  };

  const transactions = [
    {
      id: 1,
      date: "12/03/2025",
      dueDate: "15/03/2025",
      description: "Sessão realizada",
      value: "R$ 150,00",
      status: "Pago",
      method: "PIX",
    },
    {
      id: 2,
      date: "05/03/2025",
      dueDate: "15/03/2025",
      description: "Sessão realizada",
      value: "R$ 150,00",
      status: "Pago",
      method: "PIX",
    },
    {
      id: 3,
      date: "26/02/2025",
      dueDate: "15/03/2025",
      description: "Sessão - Falta",
      value: "R$ 150,00",
      status: "Atrasado",
      method: "-",
    },
    {
      id: 4,
      date: "19/02/2025",
      dueDate: "15/02/2025",
      description: "Sessão realizada",
      value: "R$ 150,00",
      status: "Pendente",
      method: "Dinheiro",
    },
  ];

  const getStatusBadge = (status: string) => {
    const styles = {
      Pago: "bg-psipro-success/20 text-psipro-success",
      Pendente: "bg-psipro-warning/20 text-psipro-warning",
      Atrasado: "bg-psipro-error/20 text-psipro-error",
    };
    return styles[status as keyof typeof styles] || styles.Pendente;
  };

  return (
    <div>
      <div className="mb-6">
        <h2 className="text-xl font-semibold text-psipro-text mb-1">Financeiro do Paciente</h2>
        <p className="text-sm text-psipro-text-secondary">
          Configurações financeiras e histórico de cobranças
        </p>
      </div>

      {/* Configurações Financeiras */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm mb-8">
        <div className="p-6 border-b border-psipro-divider">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-psipro-text">Configurações</h3>
            {!isEditing ? (
              <button
                onClick={() => setIsEditing(true)}
                className="px-4 py-2 text-sm font-medium text-psipro-primary bg-psipro-primary/10 border border-psipro-primary/30 rounded-lg hover:bg-psipro-primary/20 transition-all"
              >
                Editar
              </button>
            ) : (
              <div className="flex gap-2">
                <button
                  onClick={() => setIsEditing(false)}
                  className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
                >
                  Cancelar
                </button>
                <button
                  onClick={() => setIsEditing(false)}
                  className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all"
                >
                  Salvar
                </button>
              </div>
            )}
          </div>
        </div>
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                Valor da sessão
              </label>
              {isEditing ? (
                <input
                  type="text"
                  defaultValue={financialSettings.valorSessao}
                  className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text font-semibold">{financialSettings.valorSessao}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                Dia de cobrança
              </label>
              {isEditing ? (
                <input
                  type="number"
                  min="1"
                  max="31"
                  defaultValue={financialSettings.diaCobranca}
                  className="w-full px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none"
                />
              ) : (
                <p className="text-psipro-text font-semibold">Dia {financialSettings.diaCobranca}</p>
              )}
            </div>
            <div>
              <label className="block text-sm font-medium text-psipro-text-secondary mb-1.5">
                Lembretes
              </label>
              {isEditing ? (
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    defaultChecked={financialSettings.lembretes}
                    className="w-4 h-4 text-psipro-primary border-psipro-border rounded focus:ring-psipro-primary"
                  />
                  <span className="text-psipro-text text-sm">Enviar lembretes de cobrança</span>
                </label>
              ) : (
                <p className="text-psipro-text">
                  {financialSettings.lembretes ? "✓ Ativado" : "Desativado"}
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Resumo financeiro */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-5 mb-8">
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 shadow-sm">
          <p className="text-sm text-psipro-text-secondary mb-2 font-medium">Total pago</p>
          <p className="text-2xl font-bold text-psipro-success">{summary.totalPaid}</p>
        </div>
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 shadow-sm">
          <p className="text-sm text-psipro-text-secondary mb-2 font-medium">Pendente</p>
          <p className="text-2xl font-bold text-psipro-warning">{summary.totalPending}</p>
        </div>
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 shadow-sm">
          <p className="text-sm text-psipro-text-secondary mb-2 font-medium">Atrasado</p>
          <p className="text-2xl font-bold text-psipro-error">{summary.totalAtrasado}</p>
        </div>
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 shadow-sm">
          <p className="text-sm text-psipro-text-secondary mb-2 font-medium">Sessões realizadas</p>
          <p className="text-2xl font-bold text-psipro-text">{summary.totalSessions}</p>
        </div>
      </div>

      {/* Lista de transações */}
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-border">
          <h3 className="text-lg font-semibold text-psipro-text">Histórico de cobranças</h3>
        </div>
        {transactions.length > 0 ? (
          <div className="divide-y divide-psipro-divider">
            {transactions.map((transaction) => (
              <div
                key={transaction.id}
                className="p-6 hover:bg-psipro-surface transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <p className="text-sm font-medium text-psipro-text">
                        {transaction.description}
                      </p>
                      <span
                        className={`inline-flex px-2.5 py-1 text-xs font-medium rounded-full ${getStatusBadge(
                          transaction.status
                        )}`}
                      >
                        {transaction.status}
                      </span>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-psipro-text-muted">
                      <span>Sessão: {transaction.date}</span>
                      <span>•</span>
                      <span>Vencimento: {transaction.dueDate}</span>
                      <span>•</span>
                      <span>{transaction.method}</span>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="text-base font-semibold text-psipro-text">
                      {transaction.value}
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-12 text-center">
            <div className="text-5xl mb-4 opacity-60">💰</div>
            <p className="text-psipro-text-secondary text-base mb-2 font-medium">
              Ainda não há transações registradas.
            </p>
            <p className="text-psipro-text-muted text-sm">
              As cobranças e pagamentos aparecerão aqui.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

