"use client";

import { useState, useRef } from "react";
import * as XLSX from "xlsx";

interface ImportPatientsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onImport: (file: File, mapping: ColumnMapping) => Promise<void>;
}

type Step = "upload" | "preview" | "mapping" | "validation" | "confirmation";

interface ExcelRow {
  [key: string]: any;
}

interface ColumnMapping {
  nome: string;
  cpf: string;
  telefone: string;
  email: string;
  dataNascimento: string;
  [key: string]: string;
}

export default function ImportPatientsModal({
  isOpen,
  onClose,
  onImport,
}: ImportPatientsModalProps) {
  const [step, setStep] = useState<Step>("upload");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [excelData, setExcelData] = useState<ExcelRow[]>([]); // Todos os dados
  const [excelDataPreview, setExcelDataPreview] = useState<ExcelRow[]>([]); // Apenas preview
  const [headers, setHeaders] = useState<string[]>([]);
  const [mapping, setMapping] = useState<ColumnMapping>({
    nome: "",
    cpf: "",
    telefone: "",
    email: "",
    dataNascimento: "",
  });
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [importedCount, setImportedCount] = useState(0);
  const [importing, setImporting] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const requiredFields = ["nome", "telefone"];

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.name.endsWith(".xlsx") && !file.name.endsWith(".xls")) {
      alert("Por favor, selecione um arquivo Excel (.xlsx ou .xls)");
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        setSelectedFile(file);
        const data = new Uint8Array(e.target?.result as ArrayBuffer);
        const workbook = XLSX.read(data, { type: "array" });
        const firstSheet = workbook.Sheets[workbook.SheetNames[0]];
        
        // Obter o range completo da planilha
        const range = firstSheet['!ref'] ? XLSX.utils.decode_range(firstSheet['!ref']) : null;
        
        if (!range) {
          alert("Não foi possível ler o arquivo Excel. Verifique se o arquivo está correto.");
          return;
        }
        
        // Ler TODAS as células da primeira linha para pegar os cabeçalhos
        const sheetHeaders: string[] = [];
        const maxCol = range.e.c; // Última coluna com dados
        
        for (let col = 0; col <= maxCol; col++) {
          const cellAddress = XLSX.utils.encode_cell({ r: 0, c: col });
          const cell = firstSheet[cellAddress];
          let headerValue = "";
          
          if (cell && cell.v !== undefined && cell.v !== null) {
            headerValue = String(cell.v).trim();
          }
          
          // Se o cabeçalho estiver vazio, usar nome da coluna (A, B, C, etc.)
          if (headerValue === "") {
            const colLetter = XLSX.utils.encode_col(col);
            headerValue = `Coluna ${colLetter}`;
          }
          
          sheetHeaders.push(headerValue);
        }
        
        if (sheetHeaders.length === 0) {
          alert("Não foi possível identificar os cabeçalhos do arquivo Excel.");
          return;
        }

        // Ler todos os dados como array
        const rawData = XLSX.utils.sheet_to_json(firstSheet, { 
          header: 1, 
          defval: "",
          blankrows: false
        }) as any[][];
        
        if (rawData.length === 0) {
          alert("O arquivo Excel está vazio ou não contém dados válidos.");
          return;
        }

        // Converter para objetos usando os cabeçalhos
        const jsonData: ExcelRow[] = rawData.slice(1).map((row: any[]) => {
          const rowObj: ExcelRow = {};
          sheetHeaders.forEach((header, index) => {
            rowObj[header] = row[index] !== undefined && row[index] !== null ? String(row[index]) : "";
          });
          return rowObj;
        }).filter((row: ExcelRow) => {
          // Remover linhas completamente vazias
          return Object.values(row).some((val) => val !== "" && val !== null && val !== undefined);
        });

        if (jsonData.length === 0) {
          alert("O arquivo Excel não contém dados válidos.");
          return;
        }

        // Debug: mostrar colunas encontradas
        console.log("Colunas encontradas no Excel:", sheetHeaders);

        setHeaders(sheetHeaders);
        setExcelData(jsonData); // Todos os dados para importação
        setExcelDataPreview(jsonData.slice(0, 10)); // Preview das primeiras 10 linhas
        setStep("preview");
      } catch (error) {
        alert("Erro ao ler o arquivo Excel. Verifique se o arquivo está correto.");
        console.error(error);
      }
    };
    reader.readAsArrayBuffer(file);
  };

  const handleMappingChange = (field: string, value: string) => {
    setMapping((prev) => ({ ...prev, [field]: value }));
  };

  const validateMapping = () => {
    const errors: string[] = [];
    requiredFields.forEach((field) => {
      if (!mapping[field] || mapping[field].trim() === "") {
        errors.push(`O campo "${field}" é obrigatório e precisa ser mapeado.`);
      }
    });
    setValidationErrors(errors);
    return errors.length === 0;
  };

  const handleValidate = () => {
    if (!validateMapping()) {
      return;
    }

    // Validação básica dos dados
    const errors: string[] = [];
    excelData.forEach((row, index) => {
      const nome = row[mapping.nome];
      const telefone = row[mapping.telefone];

      if (!nome || nome.toString().trim() === "") {
        errors.push(`Linha ${index + 2}: Nome é obrigatório`);
      }
      if (!telefone || telefone.toString().trim() === "") {
        errors.push(`Linha ${index + 2}: Telefone é obrigatório`);
      }
    });

    if (errors.length > 0) {
      setValidationErrors(errors);
      return;
    }

    setValidationErrors([]);
    setStep("confirmation");
  };

  const handleConfirmImport = () => {
    if (!selectedFile) {
      setValidationErrors(["Arquivo não encontrado. Selecione o Excel novamente."]);
      setStep("upload");
      return;
    }

    setImporting(true);
    setValidationErrors([]);

    const run = async () => {
      try {
        await onImport(selectedFile, mapping);
        setImportedCount(excelData.length);
        handleClose();
      } catch (err: any) {
        setValidationErrors([err?.message || "Erro ao importar pacientes"]);
      } finally {
        setImporting(false);
      }
    };

    void run();
  };

  const handleClose = () => {
    setStep("upload");
    setSelectedFile(null);
    setExcelData([]);
    setExcelDataPreview([]);
    setHeaders([]);
    setMapping({
      nome: "",
      cpf: "",
      telefone: "",
      email: "",
      dataNascimento: "",
    });
    setValidationErrors([]);
    setImportedCount(0);
    setImporting(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
    onClose();
  };

  const autoMapColumns = () => {
    const autoMapping: ColumnMapping = {
      nome: "",
      cpf: "",
      telefone: "",
      email: "",
      dataNascimento: "",
    };

    headers.forEach((header) => {
      const lowerHeader = header.toLowerCase();
      if (lowerHeader.includes("nome") || lowerHeader.includes("name")) {
        autoMapping.nome = header;
      } else if (lowerHeader.includes("cpf")) {
        autoMapping.cpf = header;
      } else if (
        lowerHeader.includes("telefone") ||
        lowerHeader.includes("phone") ||
        lowerHeader.includes("celular")
      ) {
        autoMapping.telefone = header;
      } else if (lowerHeader.includes("email") || lowerHeader.includes("e-mail")) {
        autoMapping.email = header;
      } else if (
        lowerHeader.includes("nascimento") ||
        lowerHeader.includes("birth") ||
        lowerHeader.includes("data")
      ) {
        autoMapping.dataNascimento = header;
      }
    });

    setMapping(autoMapping);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-psipro-overlay">
      <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="p-6 border-b border-psipro-divider flex items-center justify-between">
          <div>
            <h2 className="text-xl font-semibold text-psipro-text">Importar Pacientes</h2>
            <p className="text-sm text-psipro-text-secondary mt-1">
              {step === "upload" && "Selecione o arquivo Excel com os dados dos pacientes"}
              {step === "preview" && "Visualize os dados do arquivo"}
              {step === "mapping" && "Mapeie as colunas do Excel para os campos do sistema"}
              {step === "validation" && "Valide os dados antes de importar"}
              {step === "confirmation" && "Confirme a importação"}
            </p>
          </div>
          <button
            onClick={handleClose}
            className="p-2 text-psipro-text-muted hover:text-psipro-text hover:bg-psipro-surface rounded-lg transition-colors"
          >
            ✕
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          {/* Step 1: Upload */}
          {step === "upload" && (
            <div>
              <div className="border-2 border-dashed border-psipro-border rounded-lg p-12 text-center hover:border-psipro-primary transition-all duration-200 bg-psipro-surface">
                <div className="text-5xl mb-4 opacity-60">📊</div>
                <p className="text-psipro-text-secondary font-medium mb-2 text-base">
                  Arraste o arquivo Excel aqui ou clique para selecionar
                </p>
                <p className="text-sm text-psipro-text-muted mb-4">
                  Formatos aceitos: .xlsx, .xls
                </p>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept=".xlsx,.xls"
                  onChange={handleFileUpload}
                  className="hidden"
                  id="excel-upload"
                />
                <label
                  htmlFor="excel-upload"
                  className="inline-block px-5 py-2.5 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-colors shadow-sm cursor-pointer"
                >
                  Selecionar arquivo
                </label>
              </div>
            </div>
          )}

          {/* Step 2: Preview */}
          {step === "preview" && (
            <div>
              <div className="mb-4 flex items-center justify-between">
                <p className="text-sm text-psipro-text-secondary">
                  Preview das primeiras 10 linhas do arquivo
                </p>
                <button
                  onClick={autoMapColumns}
                  className="px-4 py-2 text-sm font-medium text-psipro-primary bg-psipro-primary/10 border border-psipro-primary/30 rounded-lg hover:bg-psipro-primary/20 transition-all"
                >
                  Mapear automaticamente
                </button>
              </div>
              <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                  <thead>
                    <tr className="bg-psipro-surface border-b border-psipro-border">
                      {headers.map((header, index) => (
                        <th
                          key={index}
                          className="px-4 py-3 text-left text-xs font-semibold text-psipro-text-secondary uppercase"
                        >
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {excelDataPreview.map((row, rowIndex) => (
                      <tr
                        key={rowIndex}
                        className="border-b border-psipro-divider hover:bg-psipro-surface"
                      >
                        {headers.map((header, colIndex) => (
                          <td
                            key={colIndex}
                            className="px-4 py-3 text-sm text-psipro-text-secondary"
                          >
                            {row[header]?.toString() || "-"}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div className="mt-6 flex justify-end gap-3">
                <button
                  onClick={() => setStep("upload")}
                  className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
                >
                  Voltar
                </button>
                <button
                  onClick={() => setStep("mapping")}
                  className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all"
                >
                  Continuar
                </button>
              </div>
            </div>
          )}

          {/* Step 3: Mapping */}
          {step === "mapping" && (
            <div>
              <p className="text-sm text-psipro-text-secondary mb-6">
                Selecione qual coluna do Excel corresponde a cada campo do sistema:
              </p>
              <div className="space-y-4">
                {Object.keys(mapping).map((field) => (
                  <div key={field} className="flex items-center gap-4">
                    <label className="w-32 text-sm font-medium text-psipro-text-secondary capitalize">
                      {field === "dataNascimento" ? "Data de Nascimento" : field}:
                      {requiredFields.includes(field) && (
                        <span className="text-psipro-error ml-1">*</span>
                      )}
                    </label>
                    <select
                      value={mapping[field]}
                      onChange={(e) => handleMappingChange(field, e.target.value)}
                      aria-label={`Mapear coluna para ${field}`}
                      className="flex-1 px-3 py-2 border border-psipro-border rounded-lg bg-psipro-background text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm"
                    >
                      <option value="">Selecione a coluna...</option>
                      {headers.map((header) => (
                        <option key={header} value={header}>
                          {header}
                        </option>
                      ))}
                    </select>
                  </div>
                ))}
              </div>
              {validationErrors.length > 0 && (
                <div className="mt-4 p-4 bg-psipro-error/10 border border-psipro-error/30 rounded-lg">
                  <p className="text-sm font-medium text-psipro-error mb-2">
                    Por favor, corrija os seguintes erros:
                  </p>
                  <ul className="list-disc list-inside text-sm text-psipro-error">
                    {validationErrors.map((error, index) => (
                      <li key={index}>{error}</li>
                    ))}
                  </ul>
                </div>
              )}
              <div className="mt-6 flex justify-end gap-3">
                <button
                  onClick={() => setStep("preview")}
                  className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
                >
                  Voltar
                </button>
                <button
                  onClick={() => {
                    setStep("validation");
                    handleValidate();
                  }}
                  className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all"
                >
                  Validar
                </button>
              </div>
            </div>
          )}

          {/* Step 4: Validation */}
          {step === "validation" && (
            <div>
              {validationErrors.length > 0 ? (
                <div>
                  <div className="p-4 bg-psipro-error/10 border border-psipro-error/30 rounded-lg mb-6">
                    <p className="text-sm font-medium text-psipro-error mb-2">
                      Foram encontrados erros na validação:
                    </p>
                    <ul className="list-disc list-inside text-sm text-psipro-error space-y-1 max-h-60 overflow-y-auto">
                      {validationErrors.map((error, index) => (
                        <li key={index}>{error}</li>
                      ))}
                    </ul>
                  </div>
                  <div className="flex justify-end gap-3">
                    <button
                      onClick={() => setStep("mapping")}
                      className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
                    >
                      Corrigir mapeamento
                    </button>
                  </div>
                </div>
              ) : (
                <div>
                  <div className="p-6 bg-psipro-success/10 border border-psipro-success/30 rounded-lg mb-6 text-center">
                    <div className="text-4xl mb-2">✓</div>
                    <p className="text-sm font-medium text-psipro-success">
                      Validação concluída com sucesso!
                    </p>
                    <p className="text-sm text-psipro-text-secondary mt-1">
                      {excelData.length} pacientes prontos para importar
                    </p>
                  </div>
                  <div className="flex justify-end gap-3">
                    <button
                      onClick={() => setStep("mapping")}
                      className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
                    >
                      Voltar
                    </button>
                    <button
                      onClick={() => setStep("confirmation")}
                      className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all"
                    >
                      Continuar
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Step 5: Confirmation */}
          {step === "confirmation" && (
            <div>
              <div className="p-6 bg-psipro-primary/10 border border-psipro-primary/30 rounded-lg mb-6">
                <p className="text-sm font-medium text-psipro-text mb-4">
                  Confirme a importação dos seguintes pacientes:
                </p>
                <div className="space-y-2 max-h-60 overflow-y-auto mb-4">
                  {excelData.map((row, index) => (
                    <div
                      key={index}
                      className="p-3 bg-psipro-surface rounded border border-psipro-border"
                    >
                      <p className="text-sm font-medium text-psipro-text">
                        {row[mapping.nome]?.toString() || "Sem nome"}
                      </p>
                      <p className="text-xs text-psipro-text-muted">
                        CPF: {row[mapping.cpf]?.toString() || "-"} • Tel:{" "}
                        {row[mapping.telefone]?.toString() || "-"}
                      </p>
                    </div>
                  ))}
                </div>
                <div className="bg-psipro-surface rounded-lg p-3 border border-psipro-border">
                  <p className="text-xs text-psipro-text-muted">
                    Os pacientes importados ficam disponíveis no app e na web para gestão centralizada.
                  </p>
                </div>
              </div>
              <div className="flex justify-end gap-3">
                <button
                  onClick={() => setStep("validation")}
                  className="px-4 py-2 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all"
                >
                  Voltar
                </button>
                <button
                  onClick={handleConfirmImport}
                  className="px-4 py-2 text-sm font-medium text-psipro-text bg-psipro-primary rounded-lg hover:bg-psipro-primary-dark transition-all"
                  disabled={importing}
                >
                  {importing ? "Importando..." : "Confirmar Importação"}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

