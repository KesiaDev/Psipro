"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useClinic } from "../contexts/ClinicContext";
import { clinicService, type Clinic } from "../services/clinicService";
import { useToast } from "../contexts/ToastContext";
import { SkeletonList, SkeletonCard } from "../components/Skeleton";
import RoleBadge from "../components/RoleBadge";

const TOKEN_KEY = "psipro_token";

export default function ClinicaPage() {
  const router = useRouter();
  const { clinics, loading: contextLoading, refreshClinics } = useClinic();
  const { showSuccess, showError } = useToast();
  const [selectedClinic, setSelectedClinic] = useState<Clinic | null>(null);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newClinicName, setNewClinicName] = useState("");
  const [creating, setCreating] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editClinicName, setEditClinicName] = useState("");
  const [editing, setEditing] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const loadClinicDetails = async (clinicId: string) => {
    setLoadingDetails(true);
    try {
      const details = await clinicService.getClinicById(clinicId);
      setSelectedClinic(details);
    } catch (err: any) {
      showError(err.message || "Erro ao carregar detalhes da clínica");
    } finally {
      setLoadingDetails(false);
    }
  };

  const handleInviteUser = async () => {
    if (!selectedClinic || !inviteEmail) return;

    try {
      await clinicService.inviteUser(selectedClinic.id, {
        email: inviteEmail,
        role: "psychologist",
      });
      showSuccess("Convite enviado com sucesso!");
      setInviteEmail("");
      setShowInviteModal(false);
      // Recarregar detalhes da clínica
      await loadClinicDetails(selectedClinic.id);
      await refreshClinics();
    } catch (err: any) {
      showError(err.message || "Erro ao enviar convite");
    }
  };

  const handleSelectClinic = (clinic: Clinic) => {
    setSelectedClinic(clinic);
    if (clinic.members === undefined) {
      loadClinicDetails(clinic.id);
    }
  };

  const canManageClinic = selectedClinic && ['owner', 'admin'].includes(selectedClinic.role || "");

  const handleEditClinic = async () => {
    if (!selectedClinic || !editClinicName.trim()) return;
    setEditing(true);
    try {
      await clinicService.updateClinic(selectedClinic.id, { name: editClinicName.trim() });
      showSuccess("Clínica atualizada!");
      setShowEditModal(false);
      await loadClinicDetails(selectedClinic.id);
      await refreshClinics();
    } catch (err: any) {
      showError(err?.message || "Erro ao atualizar clínica");
    } finally {
      setEditing(false);
    }
  };

  const handleDeleteClinic = async () => {
    if (!selectedClinic) return;
    setDeleting(true);
    try {
      await clinicService.deleteClinic(selectedClinic.id);
      showSuccess("Clínica excluída.");
      setShowDeleteConfirm(false);
      setSelectedClinic(null);
      await refreshClinics();
      const activeId = typeof window !== "undefined" ? localStorage.getItem("active_clinic_id") : null;
      if (activeId === selectedClinic.id) {
        localStorage.removeItem("active_clinic_id");
      }
    } catch (err: any) {
      showError(err?.message || "Erro ao excluir clínica");
    } finally {
      setDeleting(false);
    }
  };

  const handleCreateClinic = async () => {
    const name = newClinicName.trim();
    console.log("[handleCreateClinic] Iniciando criação");
    console.log("[handleCreateClinic] Nome:", name);
    console.log("[handleCreateClinic] Token:", typeof window !== "undefined" ? localStorage.getItem(TOKEN_KEY) : null);
    if (!name) {
      showError("Informe o nome da clínica");
      return;
    }
    const token = typeof window !== "undefined" ? localStorage.getItem(TOKEN_KEY) : null;
    setCreating(true);
    try {
      console.log("[handleCreateClinic] Chamando POST /clinics...");
      const data = await clinicService.createClinic({ name });
      console.log("[handleCreateClinic] Resposta createClinic:", data);
      if (data.clinic?.id && typeof window !== "undefined") {
        localStorage.setItem("active_clinic_id", data.clinic.id);
      }
      setShowCreateModal(false);
      setNewClinicName("");
      await refreshClinics();
      showSuccess("Clínica criada com sucesso!");
      router.push("/dashboard");
    } catch (err: any) {
      console.error("[handleCreateClinic] Erro criar clínica:", err);
      const msg = err?.message || (typeof err === "string" ? err : JSON.stringify(err));
      const status = err?.status != null ? ` (HTTP ${err.status})` : "";
      showError(`Erro ao criar clínica${status}: ${msg}`);
    } finally {
      setCreating(false);
    }
  };


  if (contextLoading) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-8">
        <SkeletonList count={3} />
      </div>
    );
  }

  const CreateClinicModal = () =>
    showCreateModal ? (
      <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div className="bg-psipro-surface-elevated border border-psipro-border rounded-2xl p-6 sm:p-8 max-w-md w-full shadow-2xl">
          <h3 className="text-xl font-bold text-psipro-text mb-2">
            Criar nova clínica
          </h3>
          <p className="text-psipro-text-secondary text-sm mb-6">
            Dê um nome ao seu consultório para começar
          </p>
          <div className="mb-6">
            <label className="block text-sm font-medium text-psipro-text mb-2">
              Nome
            </label>
            <input
              type="text"
              value={newClinicName}
              onChange={(e) => setNewClinicName(e.target.value)}
              placeholder="Ex: Consultório Dr. Silva"
              className="w-full px-4 py-3 bg-psipro-background border border-psipro-border rounded-xl text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary focus:border-transparent"
              disabled={creating}
            />
          </div>
          <div className="flex gap-3">
            <button
              onClick={handleCreateClinic}
              disabled={creating || !newClinicName.trim()}
              className="flex-1 px-4 py-3 bg-psipro-primary text-white rounded-xl font-medium hover:bg-psipro-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {creating ? "Criando..." : "Criar"}
            </button>
            <button
              onClick={() => {
                setShowCreateModal(false);
                setNewClinicName("");
              }}
              disabled={creating}
              className="flex-1 px-4 py-3 bg-psipro-surface border border-psipro-border rounded-xl font-medium hover:bg-psipro-surface-elevated transition-colors"
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    ) : null;

  if (clinics.length === 0) {
    return (
      <>
        <div className="max-w-2xl mx-auto px-4 py-16 sm:py-24 text-center">
          <div className="w-20 h-20 mx-auto mb-6 rounded-2xl bg-psipro-primary/15 flex items-center justify-center text-4xl">
            🏥
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold text-psipro-text mb-3">
            Nenhuma clínica ainda
          </h1>
          <p className="text-psipro-text-secondary mb-10 text-lg">
            Crie seu primeiro consultório ou aceite um convite para entrar em uma equipe.
          </p>
          <button
            onClick={() => setShowCreateModal(true)}
            className="inline-flex items-center gap-2 px-8 py-4 bg-psipro-primary text-white rounded-xl font-medium shadow-lg shadow-psipro-primary/20 hover:bg-psipro-primary-dark transition-all"
          >
            <span className="text-xl">+</span> Criar minha primeira clínica
          </button>
        </div>
        <CreateClinicModal />
      </>
    );
  }

  const isSelected = (clinic: Clinic) => selectedClinic?.id === clinic.id;

  return (
    <>
      <div className="max-w-6xl mx-auto px-4 sm:px-6 py-8 sm:py-10">
        {/* Header */}
        <div className="mb-10">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-3xl sm:text-4xl font-bold text-psipro-text tracking-tight">
                Minhas Clínicas
              </h1>
              <p className="text-psipro-text-secondary mt-1.5 text-base sm:text-lg">
                Gerencie seus consultórios e equipe
              </p>
            </div>
            <button
              onClick={() => setShowCreateModal(true)}
              className="inline-flex items-center justify-center gap-2 px-5 py-2.5 bg-psipro-primary text-white rounded-xl font-medium shadow-lg shadow-psipro-primary/20 hover:bg-psipro-primary-dark hover:shadow-psipro-primary/30 transition-all duration-200 shrink-0"
            >
              <span className="text-lg">+</span>
              Nova Clínica
            </button>
          </div>
        </div>

        {/* Clinic cards grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-5">
          {clinics.map((clinic) => (
            <div
              key={clinic.id}
              onClick={() => handleSelectClinic(clinic)}
              className={`group relative rounded-2xl p-5 sm:p-6 cursor-pointer transition-all duration-300
                ${isSelected(clinic)
                  ? "bg-psipro-primary/10 border-2 border-psipro-primary shadow-lg shadow-psipro-primary/10"
                  : "bg-psipro-surface-elevated border border-psipro-border hover:border-psipro-primary/50 hover:shadow-md hover:shadow-black/5 dark:hover:shadow-black/20"
                }`}
            >
              <div className="flex items-start justify-between mb-3">
                <div className="w-12 h-12 rounded-xl bg-psipro-primary/15 flex items-center justify-center text-2xl group-hover:bg-psipro-primary/25 transition-colors">
                  🏥
                </div>
                <RoleBadge role={clinic.role || ""} />
              </div>
              <h3 className="text-lg font-bold text-psipro-text mb-1 truncate pr-2">
                {clinic.name}
              </h3>
              <p className="text-sm text-psipro-text-muted">
                {clinic.members?.length ?? 0} membro{clinic.members?.length !== 1 ? "s" : ""}
              </p>
              <div className="mt-4 pt-4 border-t border-psipro-divider">
                <span className="text-xs font-medium text-psipro-primary">
                  {isSelected(clinic) ? "Selecionada · Clique para ver detalhes" : "Clique para ver detalhes"}
                </span>
              </div>
            </div>
          ))}
        </div>

        {/* Selected clinic panel */}
        {selectedClinic && (
          <div className="mt-10 rounded-2xl border border-psipro-border bg-psipro-surface-elevated overflow-hidden shadow-sm">
            <div className="bg-gradient-to-r from-psipro-primary/5 to-transparent p-6 sm:p-8 border-b border-psipro-divider">
              <div className="flex justify-between items-start gap-4">
                <div className="min-w-0">
                  <h2 className="text-2xl sm:text-3xl font-bold text-psipro-text mb-1">
                    {selectedClinic.name}
                  </h2>
                  {(selectedClinic.email || selectedClinic.phone) && (
                    <div className="flex flex-wrap gap-4 mt-2 text-sm text-psipro-text-secondary">
                      {selectedClinic.email && (
                        <span className="flex items-center gap-1.5">✉️ {selectedClinic.email}</span>
                      )}
                      {selectedClinic.phone && (
                        <span className="flex items-center gap-1.5">📞 {selectedClinic.phone}</span>
                      )}
                    </div>
                  )}
                </div>
                <button
                  onClick={() => setSelectedClinic(null)}
                  className="p-2 rounded-lg text-psipro-text-muted hover:text-psipro-text hover:bg-psipro-surface transition-colors"
                  aria-label="Fechar"
                >
                  ✕
                </button>
              </div>
            </div>

            <div className="p-6 sm:p-8">
              {loadingDetails ? (
                <SkeletonList count={2} />
              ) : (
                <>
                  {selectedClinic.members && selectedClinic.members.length > 0 && (
                    <div className="mb-8">
                      <h3 className="text-base font-semibold text-psipro-text mb-4 flex items-center gap-2">
                        <span>👥</span> Membros da equipe
                      </h3>
                      <div className="space-y-3">
                        {selectedClinic.members.map((member) => (
                          <div
                            key={member.id}
                            className="flex items-center justify-between gap-4 p-4 rounded-xl bg-psipro-background border border-psipro-border hover:border-psipro-primary/30 transition-colors"
                          >
                            <div className="flex items-center gap-4 min-w-0">
                              <div className="w-10 h-10 rounded-full bg-psipro-primary/20 flex items-center justify-center text-psipro-primary font-bold shrink-0">
                                {member.name?.charAt(0)?.toUpperCase() || "?"}
                              </div>
                              <div className="min-w-0">
                                <div className="font-semibold text-psipro-text truncate">
                                  {member.name}
                                </div>
                                <div className="text-sm text-psipro-text-secondary truncate">
                                  {member.email}
                                </div>
                                <div className="mt-1">
                                  <RoleBadge role={member.role} />
                                </div>
                              </div>
                            </div>
                            <span className={`px-3 py-1 text-xs font-medium rounded-full shrink-0
                              ${member.status?.toLowerCase() === "active" 
                                ? "bg-psipro-success/15 text-psipro-success" 
                                : "bg-psipro-surface text-psipro-text-muted"
                              }`}
                            >
                              {member.status?.toLowerCase() === "active" ? "Ativo" : member.status || "—"}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Action buttons */}
                  <div className="flex flex-wrap gap-3">
                    {selectedClinic.permissions?.canManageUsers && (
                      <>
                        <button
                          onClick={() => setShowInviteModal(true)}
                          className="inline-flex items-center gap-2 px-4 py-2.5 bg-psipro-primary text-white rounded-xl font-medium hover:bg-psipro-primary-dark transition-colors"
                        >
                          <span>✉️</span> Convidar Membro
                        </button>
                        <button
                          onClick={() => router.push("/dashboard")}
                          className="inline-flex items-center gap-2 px-4 py-2.5 bg-psipro-surface border border-psipro-border rounded-xl font-medium hover:bg-psipro-surface-elevated hover:border-psipro-primary/30 transition-colors"
                        >
                          <span>📊</span> Ver Dashboard
                        </button>
                      </>
                    )}
                    {canManageClinic && (
                      <>
                        <button
                          onClick={() => {
                            setEditClinicName(selectedClinic?.name || "");
                            setShowEditModal(true);
                          }}
                          className="inline-flex items-center gap-2 px-4 py-2.5 bg-psipro-surface border border-psipro-border rounded-xl font-medium hover:bg-psipro-surface-elevated hover:border-psipro-primary/30 transition-colors"
                        >
                          <span>✏️</span> Alterar
                        </button>
                        <button
                          onClick={() => setShowDeleteConfirm(true)}
                          className="inline-flex items-center gap-2 px-4 py-2.5 bg-red-500/10 text-red-600 border border-red-500/30 rounded-xl font-medium hover:bg-red-500/20 transition-colors"
                        >
                          <span>🗑️</span> Excluir
                        </button>
                      </>
                    )}
                  </div>
                </>
              )}
            </div>
          </div>
        )}

        {showEditModal && selectedClinic && (
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-psipro-surface-elevated border border-psipro-border rounded-2xl p-6 sm:p-8 max-w-md w-full shadow-2xl">
              <h3 className="text-xl font-bold text-psipro-text mb-2">Alterar clínica</h3>
              <div className="mb-6">
                <label className="block text-sm font-medium text-psipro-text mb-2">Nome</label>
                <input
                  type="text"
                  value={editClinicName}
                  onChange={(e) => setEditClinicName(e.target.value)}
                  placeholder="Ex: Consultório Dr. Silva"
                  className="w-full px-4 py-3 bg-psipro-background border border-psipro-border rounded-xl text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary focus:border-transparent"
                  disabled={editing}
                />
              </div>
              <div className="flex gap-3">
                <button
                  onClick={handleEditClinic}
                  disabled={editing || !editClinicName.trim()}
                  className="flex-1 px-4 py-3 bg-psipro-primary text-white rounded-xl font-medium hover:bg-psipro-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {editing ? "Salvando..." : "Salvar"}
                </button>
                <button
                  onClick={() => setShowEditModal(false)}
                  disabled={editing}
                  className="flex-1 px-4 py-3 bg-psipro-surface border border-psipro-border rounded-xl font-medium hover:bg-psipro-surface-elevated transition-colors"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        )}

        {showDeleteConfirm && selectedClinic && (
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-psipro-surface-elevated border border-red-500/30 rounded-2xl p-6 sm:p-8 max-w-md w-full shadow-2xl">
              <h3 className="text-xl font-bold text-psipro-text mb-2">Excluir clínica</h3>
              <p className="text-psipro-text-secondary text-sm mb-4">
                Tem certeza que deseja excluir &quot;{selectedClinic.name}&quot;? Esta ação não pode ser desfeita.
              </p>
              <div className="flex gap-3 mt-6">
                <button
                  onClick={handleDeleteClinic}
                  disabled={deleting}
                  className="flex-1 px-4 py-3 bg-red-600 text-white rounded-xl font-medium hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {deleting ? "Excluindo..." : "Excluir"}
                </button>
                <button
                  onClick={() => setShowDeleteConfirm(false)}
                  disabled={deleting}
                  className="flex-1 px-4 py-3 bg-psipro-surface border border-psipro-border rounded-xl font-medium hover:bg-psipro-surface-elevated transition-colors"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        )}

        {showInviteModal && selectedClinic && (
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
            <div className="bg-psipro-surface-elevated border border-psipro-border rounded-2xl p-6 sm:p-8 max-w-md w-full shadow-2xl">
              <h3 className="text-xl font-bold text-psipro-text mb-2">
                Convidar membro
              </h3>
              <p className="text-psipro-text-secondary text-sm mb-6">
                Envie um convite por e-mail para adicionar alguém à equipe
              </p>
              <div className="mb-6">
                <label className="block text-sm font-medium text-psipro-text mb-2">
                  E-mail
                </label>
                <input
                  type="email"
                  value={inviteEmail}
                  onChange={(e) => setInviteEmail(e.target.value)}
                  placeholder="colega@email.com"
                  className="w-full px-4 py-3 bg-psipro-background border border-psipro-border rounded-xl text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary focus:border-transparent"
                />
              </div>
              <div className="flex gap-3">
                <button
                  onClick={handleInviteUser}
                  disabled={!inviteEmail}
                  className="flex-1 px-4 py-3 bg-psipro-primary text-white rounded-xl font-medium hover:bg-psipro-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Enviar convite
                </button>
                <button
                  onClick={() => {
                    setShowInviteModal(false);
                    setInviteEmail("");
                  }}
                  className="flex-1 px-4 py-3 bg-psipro-surface border border-psipro-border rounded-xl font-medium hover:bg-psipro-surface-elevated transition-colors"
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
      <CreateClinicModal />
    </>
  );
}


