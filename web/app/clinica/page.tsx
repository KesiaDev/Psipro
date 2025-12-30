"use client";

import { useState } from "react";
import { useClinic } from "../contexts/ClinicContext";
import { clinicService, type Clinic } from "../services/clinicService";
import { useToast } from "../contexts/ToastContext";
import { SkeletonList, SkeletonCard } from "../components/Skeleton";
import RoleBadge from "../components/RoleBadge";

export default function ClinicaPage() {
  const { clinics, loading: contextLoading, refreshClinics } = useClinic();
  const { showSuccess, showError } = useToast();
  const [selectedClinic, setSelectedClinic] = useState<Clinic | null>(null);
  const [loadingDetails, setLoadingDetails] = useState(false);
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");

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


  if (contextLoading) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-8">
        <SkeletonList count={3} />
      </div>
    );
  }

  if (clinics.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-psipro-text mb-4">
            Você ainda não faz parte de nenhuma clínica
          </h1>
          <p className="text-psipro-text-secondary mb-8">
            Crie uma clínica ou aceite um convite para começar.
          </p>
          <button className="px-6 py-3 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors">
            Criar Clínica
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-psipro-text mb-2">Clínicas</h1>
        <p className="text-psipro-text-secondary">
          Gerencie suas clínicas e equipe
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {clinics.map((clinic) => (
          <div
            key={clinic.id}
            onClick={() => handleSelectClinic(clinic)}
            className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 cursor-pointer hover:border-psipro-primary transition-colors"
          >
            <h3 className="text-xl font-semibold text-psipro-text mb-2">
              {clinic.name}
            </h3>
            <div className="text-sm text-psipro-text-secondary mb-4">
              {clinic.members && (
                <div>{clinic.members.length} membro(s)</div>
              )}
            </div>
            <div className="flex gap-2">
              <RoleBadge role={clinic.role || ""} />
            </div>
          </div>
        ))}
      </div>

      {selectedClinic && (
        <div className="mt-8 bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h2 className="text-2xl font-bold text-psipro-text mb-2">
                {selectedClinic.name}
              </h2>
              {selectedClinic.email && (
                <div className="text-sm text-psipro-text-secondary">
                  {selectedClinic.email}
                </div>
              )}
              {selectedClinic.phone && (
                <div className="text-sm text-psipro-text-secondary">
                  {selectedClinic.phone}
                </div>
              )}
            </div>
            <button
              onClick={() => setSelectedClinic(null)}
              className="text-psipro-text-secondary hover:text-psipro-text transition-colors"
            >
              ✕
            </button>
          </div>

          {loadingDetails ? (
            <SkeletonList count={2} />
          ) : (
            <>
              {selectedClinic.members && selectedClinic.members.length > 0 && (
                <div className="mb-6">
                  <h3 className="text-lg font-semibold text-psipro-text mb-4">
                    Membros
                  </h3>
                  <div className="space-y-2">
                    {selectedClinic.members.map((member) => (
                      <div
                        key={member.id}
                        className="flex justify-between items-center p-3 bg-psipro-background rounded-lg"
                      >
                        <div>
                          <div className="font-medium text-psipro-text">
                            {member.name}
                          </div>
                          <div className="text-sm text-psipro-text-secondary flex items-center gap-2">
                            <span>{member.email}</span>
                            <RoleBadge role={member.role} />
                          </div>
                        </div>
                        <span className="px-2 py-1 text-xs bg-psipro-success/10 text-psipro-success rounded capitalize">
                          {member.status}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {selectedClinic.permissions?.canManageUsers && (
                <div className="flex gap-4">
                  <button
                    onClick={() => setShowInviteModal(true)}
                    className="px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors"
                  >
                    Convidar Membro
                  </button>
                  <button className="px-4 py-2 bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-colors">
                    Ver Dashboard
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}

      {showInviteModal && selectedClinic && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-psipro-surface-elevated border border-psipro-border rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-xl font-bold text-psipro-text mb-4">
              Convidar Membro
            </h3>
            <div className="mb-4">
              <label className="block text-sm font-medium text-psipro-text mb-2">
                Email do usuário
              </label>
              <input
                type="email"
                value={inviteEmail}
                onChange={(e) => setInviteEmail(e.target.value)}
                placeholder="usuario@email.com"
                className="w-full px-4 py-2 bg-psipro-background border border-psipro-border rounded-lg text-psipro-text focus:outline-none focus:ring-2 focus:ring-psipro-primary"
              />
            </div>
            <div className="flex gap-4">
              <button
                onClick={handleInviteUser}
                disabled={!inviteEmail}
                className="flex-1 px-4 py-2 bg-psipro-primary text-white rounded-lg hover:bg-psipro-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Enviar Convite
              </button>
              <button
                onClick={() => {
                  setShowInviteModal(false);
                  setInviteEmail("");
                }}
                className="flex-1 px-4 py-2 bg-psipro-surface border border-psipro-border rounded-lg hover:bg-psipro-surface-elevated transition-colors"
              >
                Cancelar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}


