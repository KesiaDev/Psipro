"use client";

import { useState, useEffect } from "react";
import { documentService, type Document } from "@/app/services/documentService";
import Skeleton from "@/app/components/Skeleton";

function formatDate(iso?: string) {
  if (!iso) return "—";
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

export default function DocumentosArquivosTab({
  patientId,
}: {
  patientId: string;
}) {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    documentService
      .getByPatient(patientId)
      .then((d) => {
        if (!cancelled) setDocuments(d);
      })
      .catch((e) => {
        if (!cancelled) {
          setError(
            e && typeof e === "object" && "message" in e
              ? String((e as { message: string }).message)
              : "Erro ao carregar documentos"
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

  const handleView = (doc: Document) => {
    if (doc.fileUrl) window.open(doc.fileUrl, "_blank");
  };

  const handleDownload = (doc: Document) => {
    if (doc.fileUrl) {
      const a = document.createElement("a");
      a.href = doc.fileUrl;
      a.download = doc.name || "documento";
      a.target = "_blank";
      a.click();
    }
  };

  if (loading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-48" />
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

  return (
    <div>
      <div className="mb-4">
        <p className="text-sm text-psipro-text-secondary">
          Documentos e arquivos associados ao paciente. Clique para visualizar ou fazer download.
        </p>
      </div>

      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border">
        <div className="p-6 border-b border-psipro-divider">
          <h2 className="text-lg font-semibold text-psipro-text">Documentos & Arquivos</h2>
        </div>
        <div className="divide-y divide-psipro-divider">
          {documents.length > 0 ? (
            documents.map((doc) => (
              <div key={doc.id} className="p-6 hover:bg-psipro-surface transition-colors">
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-1">
                      <span className="text-base font-medium text-psipro-text">{doc.name}</span>
                      <span className="text-xs text-psipro-text-muted bg-psipro-surface px-2 py-1 rounded">
                        {doc.type}
                      </span>
                    </div>
                    <div className="flex items-center gap-4 text-sm text-psipro-text-secondary">
                      <span>{formatDate(doc.createdAt)}</span>
                      <span
                        className={`inline-flex px-2 py-0.5 text-xs font-medium rounded-full ${
                          doc.status === "Ativo"
                            ? "bg-psipro-success/20 text-psipro-success"
                            : "bg-psipro-text-muted/20 text-psipro-text-muted"
                        }`}
                      >
                        {doc.status}
                      </span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleView(doc)}
                      disabled={!doc.fileUrl}
                      className="px-3 py-1.5 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Visualizar
                    </button>
                    <button
                      onClick={() => handleDownload(doc)}
                      disabled={!doc.fileUrl}
                      className="px-3 py-1.5 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      Download
                    </button>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="p-12 text-center">
              <div className="text-5xl mb-4 opacity-40">📄</div>
              <p className="text-psipro-text-secondary text-sm mb-1">
                Nenhum documento encontrado
              </p>
              <p className="text-psipro-text-muted text-xs">
                Documentos associados ao paciente aparecerão aqui
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
