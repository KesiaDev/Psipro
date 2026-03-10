"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import ImportPatientsModal from "@/app/components/ImportPatientsModal";
import CreatePatientModal, {
  type CreatePatientFormValues,
} from "@/app/components/CreatePatientModal";
import { DASHBOARD_REFRESH_EVENT } from "@/app/constants/events";
import { patientService, type Patient } from "@/app/services/patientService";
import { useClinic } from "@/app/contexts/ClinicContext";
import { useToast } from "@/app/contexts/ToastContext";
import Skeleton from "@/app/components/Skeleton";

export default function PacientesPage() {
  const router = useRouter();
  const { currentClinic } = useClinic();
  const { showError, showSuccess } = useToast();
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [isImportModalOpen, setIsImportModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);

  const isImportEnabled = true;

  const loadPatients = async () => {
    setLoading(true);
    setPageError(null);
    try {
      const data = await patientService.getPatients();
      setPatients(data);
    } catch (error) {
      const err = error as { status?: number; message?: string };
      const status = err?.status;
      const msg =
        status === 401
          ? "Sessão expirada. Faça login novamente."
          : status === 403
            ? "Sem permissão para acessar os pacientes desta clínica."
            : err?.message || "Erro ao carregar pacientes";
      setPageError(msg);
      showError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPatients();
  }, [currentClinic?.id]);

  const handleCreatePatient = async (values: CreatePatientFormValues) => {
    setCreating(true);
    try {
      const payload = {
        name: (values.name ?? '').trim(),
        phone: (values.phone ?? '').trim() || undefined,
        email: (values.email ?? '').trim() || undefined,
        cpf: (values.cpf ?? '').trim() || undefined,
        birthDate: values.birthDate || undefined,
        status: "Ativo",
        source: "web",
      };

      await patientService.createPatient(payload);
      showSuccess("Paciente cadastrado com sucesso");
      setIsCreateModalOpen(false);
      setPageError(null);
      await loadPatients();
      if (typeof window !== "undefined") {
        window.dispatchEvent(new CustomEvent(DASHBOARD_REFRESH_EVENT));
      }
      router.push("/pacientes");
    } catch (error) {
      const err = error as { status?: number; message?: string };
      const status = err?.status;
      const msg =
        status === 401
          ? "Sessão expirada. Faça login novamente."
          : status === 403
            ? "Sem permissão para cadastrar pacientes nesta clínica."
            : err?.message || "Erro ao cadastrar paciente";
      setPageError(msg);
      showError(msg);
    } finally {
      setCreating(false);
    }
  };

  const handleImportPatients = async (file: File, mapping: Record<string, string>) => {
    try {
      await patientService.importPatients(file, mapping);
      showSuccess("Pacientes importados com sucesso");
      setPageError(null);
      await loadPatients();
      if (typeof window !== "undefined") {
        window.dispatchEvent(new CustomEvent(DASHBOARD_REFRESH_EVENT));
      }
    } catch (error) {
      const err = error as { status?: number; message?: string };
      const status = err?.status;
      const msg =
        status === 401
          ? "Sessão expirada. Faça login novamente."
          : status === 403
            ? "Sem permissão para importar pacientes nesta clínica."
            : err?.message || "Erro ao importar pacientes";
      setPageError(msg);
      showError(msg);
      throw error;
    }
  };

  const filteredPatients = patients.filter((patient) => {
    const matchesSearch =
      searchTerm === "" ||
      patient.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      patient.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      patient.phone?.includes(searchTerm);

    const matchesStatus =
      statusFilter === "all" || patient.status?.toLowerCase() === statusFilter.toLowerCase();

    return matchesSearch && matchesStatus;
  });

  const hasPatients = patients.length > 0;
  const hasFilteredResults = filteredPatients.length > 0;

  return (
    <div className="min-w-0">
      {pageError && (
        <div className="mb-6 p-4 rounded-lg bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400">
          <p className="font-medium">{pageError}</p>
          <button
            onClick={loadPatients}
            className="mt-2 text-sm underline hover:no-underline"
          >
            Tentar novamente
          </button>
        </div>
      )}
      {/* Header */}
      <div className="mb-6 sm:mb-8">
        <div className="mb-4 sm:mb-6">
          <h1 className="text-2xl sm:text-3xl font-bold text-psipro-text mb-2 tracking-tight">Pacientes</h1>
          <p className="text-psipro-text-secondary text-base sm:text-lg">
            Gestão centralizada de pacientes e prontuários
          </p>
        </div>

        {/* Barra superior de ações */}
        <div className="flex flex-wrap items-center gap-2 sm:gap-3 mb-4 sm:mb-6">
          <button
            onClick={() => setIsImportModalOpen(true)}
            disabled={!isImportEnabled}
            className={`px-5 py-2.5 text-sm font-medium rounded-lg transition-all shadow-sm ${
              isImportEnabled
                ? "text-psipro-text bg-psipro-primary hover:bg-psipro-primary-dark"
                : "text-psipro-text-secondary bg-psipro-surface border border-psipro-border opacity-70 cursor-not-allowed"
            }`}
          >
            Importar pacientes (Excel)
          </button>
          <button
            type="button"
            onClick={() => setIsCreateModalOpen(true)}
            className="px-5 py-2.5 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated hover:border-psipro-primary/30 transition-all shadow-sm cursor-pointer"
          >
            Novo paciente
          </button>
        </div>

        {/* Busca e Filtros - apenas se houver pacientes */}
        {hasPatients && (
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="relative flex-1">
              <input
                type="text"
                placeholder="Buscar por nome, e-mail ou telefone..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full px-4 py-3 pl-11 border border-psipro-border rounded-lg focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm shadow-sm bg-psipro-surface-elevated text-psipro-text"
              />
              <span className="absolute left-3.5 top-1/2 transform -translate-y-1/2 text-psipro-text-muted text-lg">
                🔍
              </span>
            </div>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              aria-label="Filtrar por status"
              className="px-4 py-3 border border-psipro-border rounded-lg bg-psipro-surface-elevated text-psipro-text focus:ring-2 focus:ring-psipro-primary focus:border-psipro-primary outline-none text-sm"
            >
              <option value="all">Todos os status</option>
              <option value="ativo">Ativo</option>
              <option value="inativo">Inativo</option>
            </select>
          </div>
        )}
      </div>


      {/* Estado vazio ou Lista de Pacientes */}
      {loading ? (
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-16">
          <Skeleton className="h-64" />
        </div>
      ) : !hasPatients ? (
        <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm p-6 sm:p-12 md:p-16 text-center">
          <div className="max-w-md mx-auto">
            <div className="text-6xl mb-6 opacity-60">👥</div>
            <h2 className="text-2xl font-semibold text-psipro-text mb-3">
              Nenhum paciente cadastrado ainda
            </h2>
            <p className="text-psipro-text-secondary text-base mb-8 leading-relaxed">
              Cadastre ou importe pacientes para começar a usar o PsiPro. No app você realiza os
              atendimentos e registra as sessões. Na web você gerencia tudo com mais conforto e visão ampla.
            </p>
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              <button
                onClick={() => setIsImportModalOpen(true)}
                disabled={!isImportEnabled}
                className={`px-6 py-3 text-sm font-medium rounded-lg transition-all shadow-sm ${
                  isImportEnabled
                    ? "text-psipro-text bg-psipro-primary hover:bg-psipro-primary-dark"
                    : "text-psipro-text-secondary bg-psipro-surface border border-psipro-border opacity-70 cursor-not-allowed"
                }`}
              >
                Importar pacientes
              </button>
              <button
                type="button"
                onClick={() => setIsCreateModalOpen(true)}
                className="px-6 py-3 text-sm font-medium text-psipro-text-secondary bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-all shadow-sm cursor-pointer"
              >
                Cadastrar manualmente
              </button>
            </div>
          </div>
        </div>
      ) : (
        <>
          {/* Estrutura de tabela */}
          <div className="bg-psipro-surface-elevated rounded-lg border border-psipro-border shadow-sm overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="bg-psipro-surface border-b border-psipro-border">
                    <th className="px-6 py-4 text-left text-xs font-semibold text-psipro-text-secondary uppercase tracking-wider">
                      Nome
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-psipro-text-secondary uppercase tracking-wider">
                      Contato
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-psipro-text-secondary uppercase tracking-wider">
                      Última sessão (app)
                    </th>
                    <th className="px-6 py-4 text-left text-xs font-semibold text-psipro-text-secondary uppercase tracking-wider">
                      Status
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-psipro-divider">
                  {hasFilteredResults ? (
                    filteredPatients.map((patient) => (
                      <tr
                        key={patient.id}
                        onClick={() => router.push(`/pacientes/${patient.id}`)}
                        className="hover:bg-psipro-surface transition-colors cursor-pointer"
                      >
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="flex items-center gap-3">
                            <div className="h-10 w-10 rounded-full bg-psipro-primary border-2 border-psipro-primary-dark flex items-center justify-center shrink-0">
                              <span className="text-psipro-text text-sm font-semibold">
                                {patient.name?.charAt(0) || "?"}
                              </span>
                            </div>
                            <div>
                              <div className="text-sm font-medium text-psipro-text">
                                {patient.name || "Sem nome"}
                              </div>
                              {patient.age && (
                                <div className="text-xs text-psipro-text-muted">
                                  {patient.age} anos
                                </div>
                              )}
                            </div>
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-psipro-text-secondary">
                            {patient.phone && (
                              <div className="mb-1">{patient.phone}</div>
                            )}
                            {patient.email && (
                              <div className="text-xs text-psipro-text-muted">{patient.email}</div>
                            )}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm text-psipro-text-secondary">
                            {patient.nextSession && patient.nextSession !== "-"
                              ? patient.nextSession
                              : "-"}
                          </div>
                          {patient.sessionsCount !== undefined && (
                            <div className="text-xs text-psipro-text-muted">
                              {patient.sessionsCount} sessões
                            </div>
                          )}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span
                            className={`inline-flex px-2.5 py-1 text-xs font-medium rounded-full ${
                              patient.status === "Ativo"
                                ? "bg-psipro-success/20 text-psipro-success"
                                : "bg-psipro-text-muted/20 text-psipro-text-muted"
                            }`}
                          >
                            {patient.status || "Indefinido"}
                          </span>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={4} className="px-6 py-12 text-center">
                        <div className="text-psipro-text-secondary text-sm">
                          {searchTerm || statusFilter !== "all"
                            ? "Nenhum paciente encontrado com os filtros aplicados."
                            : "Nenhum paciente cadastrado."}
                        </div>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Contador */}
          {hasFilteredResults && (
            <div className="mt-4 text-sm text-psipro-text-muted">
              Mostrando {filteredPatients.length} de {patients.length} pacientes
            </div>
          )}
        </>
      )}

      {/* Modal de Importação */}
      <ImportPatientsModal
        isOpen={isImportModalOpen}
        onClose={() => setIsImportModalOpen(false)}
        onImport={handleImportPatients}
      />

      {/* Modal de Cadastro Manual */}
      <CreatePatientModal
        isOpen={isCreateModalOpen}
        isSubmitting={creating}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreatePatient}
      />
    </div>
  );
}
