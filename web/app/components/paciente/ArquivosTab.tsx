export default function ArquivosTab() {
  const files = [
    {
      id: 1,
      name: "Exame_psicologico_2024.pdf",
      size: "2.4 MB",
      uploadedAt: "10/03/2025",
      type: "pdf",
    },
    {
      id: 2,
      name: "Relatorio_escola.pdf",
      size: "1.8 MB",
      uploadedAt: "05/02/2025",
      type: "pdf",
    },
  ];

  const getFileIcon = (type: string) => {
    const icons: Record<string, string> = {
      pdf: "📄",
      doc: "📝",
      docx: "📝",
      jpg: "🖼️",
      png: "🖼️",
      default: "📁",
    };
    return icons[type] || icons.default;
  };

  return (
    <div>
      {/* Área de upload */}
      <div className="mb-8">
        <div className="border-2 border-dashed border-psipro-border rounded-lg p-10 text-center hover:border-psipro-primary transition-all duration-200 bg-psipro-surface">
          <div className="text-5xl mb-4 opacity-60">📤</div>
          <p className="text-psipro-text-secondary font-medium mb-2 text-base">
            Arraste arquivos aqui ou clique para selecionar
          </p>
          <p className="text-sm text-psipro-text-muted mb-4">
            PDF, imagens, documentos (máx. 10 MB)
          </p>
          <button className="px-5 py-2.5 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-colors shadow-sm">
            Selecionar arquivos
          </button>
        </div>
      </div>

      {/* Lista de arquivos */}
      {files.length > 0 ? (
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm">
          <div className="p-6 border-b border-psipro-border">
            <h3 className="text-lg font-semibold text-psipro-text">
              Arquivos anexados ({files.length})
            </h3>
          </div>
          <div className="divide-y divide-psipro-divider">
            {files.map((file) => (
              <div
                key={file.id}
                className="p-6 hover:bg-psipro-surface transition-colors"
              >
                <div className="flex items-center gap-4">
                  <span className="text-3xl opacity-80">{getFileIcon(file.type)}</span>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-psipro-text truncate">
                      {file.name}
                    </p>
                    <div className="flex items-center gap-3 mt-1 text-xs text-psipro-text-muted">
                      <span>{file.size}</span>
                      <span>•</span>
                      <span>Enviado em {file.uploadedAt}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button className="p-2 text-psipro-text-secondary hover:bg-psipro-surface rounded-lg transition-colors" title="Visualizar">
                      👁️
                    </button>
                    <button className="p-2 text-psipro-text-secondary hover:bg-psipro-surface rounded-lg transition-colors" title="Download">
                      ⬇️
                    </button>
                    <button className="p-2 text-psipro-error hover:bg-psipro-error/10 rounded-lg transition-colors" title="Excluir">
                      🗑️
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : (
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-12 text-center">
          <div className="text-5xl mb-4 opacity-60">📁</div>
          <p className="text-psipro-text-secondary text-base mb-2 font-medium">
            Nenhum arquivo foi anexado ainda.
          </p>
          <p className="text-psipro-text-muted text-sm">
            Você pode fazer upload de documentos, exames e outros arquivos
            relacionados ao paciente.
          </p>
        </div>
      )}
    </div>
  );
}

