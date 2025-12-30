export default function DocumentosArquivosTab({ patientId }: { patientId: string }) {
  // Dados mockados
  const documents = [
    {
      id: "1",
      name: "Termo de Consentimento",
      type: "Documento",
      date: "15/01/2025",
      status: "Ativo",
    },
    {
      id: "2",
      name: "Avaliação Inicial",
      type: "Relatório",
      date: "20/01/2025",
      status: "Ativo",
    },
    {
      id: "3",
      name: "Receita Médica",
      type: "Documento",
      date: "10/02/2025",
      status: "Ativo",
    },
  ];

  return (
    <div>
      <div className="mb-4">
        <p className="text-sm text-psipro-text-secondary">
          Documentos e arquivos associados ao paciente. Clique para visualizar ou fazer download.
        </p>
      </div>

      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
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
                      <span>{doc.date}</span>
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
                    <button className="px-3 py-1.5 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all">
                      Visualizar
                    </button>
                    <button className="px-3 py-1.5 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all">
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



