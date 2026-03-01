"use client";

import { useState, useEffect } from "react";
import { financialService } from "@/app/services/financialService";
import { paymentService } from "@/app/services/paymentService";
import type { Payment } from "@/app/services/paymentService";
import Skeleton from "@/app/components/Skeleton";

function formatCurrency(value: number) {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

function formatDate(iso: string) {
  try {
    const d = new Date(iso);
    return d.toLocaleDateString("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  } catch {
    return iso;
  }
}

export default function FinanceiroPacienteTab({
  patientId,
}: {
  patientId: string;
}) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [summary, setSummary] = useState<{
    totalFaturado: number;
    totalRecebido: number;
    totalAberto: number;
  } | null>(null);
  const [payments, setPayments] = useState<Payment[]>([]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    Promise.all([
      financialService.getPatientFinancial(patientId),
      paymentService.getByPatient(patientId),
    ])
      .then(([s, p]) => {
        if (!cancelled) {
          setSummary(s);
          setPayments(Array.isArray(p) ? p : []);
        }
      })
      .catch((e) => {
        if (!cancelled) {
          setError(
            e && typeof e === "object" && "message" in e
              ? String((e as { message: string }).message)
              : "Erro ao carregar dados financeiros"
          );
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [patientId]);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
          {[1, 2, 3].map((i) => (
            <Skeleton key={i} className="h-28" />
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-psipro-error/10 border border-psipro-error/20 text-psipro-error rounded-lg p-6">
        <p className="font-medium">{error}</p>
      </div>
    );
  }

  if (!summary) return null;

  const financialCards = [
    {
      title: "Total faturado",
      value: formatCurrency(summary.totalFaturado),
      subtitle: "Total cobrado",
    },
    {
      title: "Total recebido",
      value: formatCurrency(summary.totalRecebido),
      subtitle: "Pagamentos realizados",
    },
    {
      title: "Total em aberto",
      value: formatCurrency(summary.totalAberto),
      subtitle: "Pendências",
    },
  ];

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {financialCards.map((card, index) => (
          <div
            key={index}
            className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 hover:shadow-md transition-all duration-200"
          >
            <p className="text-sm text-psipro-text-secondary mb-2 font-medium">
              {card.title}
            </p>
            <p className="text-3xl font-bold text-psipro-text mb-1">
              {card.value}
            </p>
            <p className="text-xs text-psipro-text-muted">{card.subtitle}</p>
          </div>
        ))}
      </div>

      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">
            Cobranças / Pagamentos
          </h2>
        </div>
        <div className="divide-y divide-psipro-divider">
          {payments.length === 0 ? (
            <div className="p-6 text-center text-psipro-text-secondary text-sm">
              Nenhum pagamento registrado ainda.
            </div>
          ) : (
            payments.map((charge) => (
              <div
                key={charge.id}
                className="p-6 hover:bg-psipro-surface transition-colors"
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-1">
                      <span className="text-sm font-medium text-psipro-text">
                        {formatDate(charge.date)}
                      </span>
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
                  </div>
                  <div className="text-right">
                    <p className="text-lg font-semibold text-psipro-text">
                      {formatCurrency(Number(charge.amount))}
                    </p>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6">
        <div className="flex items-center gap-3">
          <div className="text-2xl opacity-60">📱</div>
          <div className="flex-1">
            <p className="text-sm text-psipro-text-secondary">
              <strong>
                O registro de pagamentos é feito no app PsiPro.
              </strong>{" "}
              A web é usada para acompanhamento e análise.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
