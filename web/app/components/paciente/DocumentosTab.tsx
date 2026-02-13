export default function DocumentosTab() {
  const documentTypes = [
    {
      id: 1,
      name: "Termo de Consentimento",
      description: "Documento de consentimento informado para tratamento",
      icon: "📋",
    },
    {
      id: 2,
      name: "Declaração de Atendimento",
      description: "Declaração para fins diversos",
      icon: "📄",
    },
    {
      id: 3,
      name: "Encaminhamento",
      description: "Documento para encaminhamento a outros profissionais",
      icon: "📤",
    },
    {
      id: 4,
      name: "Relatório Psicológico",
      description: "Relatório detalhado do acompanhamento",
      icon: "📊",
    },
  ];

  return (
    <div>
      <div className="mb-6">
        <p className="text-psipro-text-secondary text-sm leading-relaxed">
          Modelos de documentos disponíveis para este paciente. A geração de
          documentos será implementada em breve.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {documentTypes.map((doc) => (
          <div
            key={doc.id}
            className="bg-psipro-surface-elevated rounded-lg border border-psipro-border p-6 hover:shadow-md transition-all duration-200 cursor-pointer"
          >
            <div className="flex items-start gap-4">
              <span className="text-3xl opacity-80">{doc.icon}</span>
              <div className="flex-1">
                <h3 className="text-base font-semibold text-psipro-text mb-2">
                  {doc.name}
                </h3>
                <p className="text-sm text-psipro-text-secondary leading-relaxed">
                  {doc.description}
                </p>
              </div>
            </div>
            <div className="mt-5 pt-4 border-t border-psipro-divider">
              <button className="text-sm font-medium text-psipro-primary hover:text-psipro-primary-dark transition-colors">
                Gerar documento →
              </button>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-6 bg-psipro-primary/10 border border-psipro-primary/30 rounded-lg p-4">
        <p className="text-sm text-psipro-text-secondary">
          <strong>Em breve:</strong> Você poderá gerar e personalizar documentos
          diretamente da plataforma.
        </p>
      </div>
    </div>
  );
}

