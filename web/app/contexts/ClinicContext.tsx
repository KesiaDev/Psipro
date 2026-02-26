/**
 * ⚠️ ARQUIVO CRÍTICO - INTEGRAÇÃO BACKEND
 *
 * Este arquivo contém lógica essencial de integração com API,
 * autenticação ou variáveis de ambiente.
 *
 * NÃO alterar estrutura, headers, interceptors ou contratos de API
 * durante modernização visual.
 *
 * Qualquer alteração pode quebrar produção.
 */

"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { clinicService } from "../services/clinicService";
import type { Clinic } from "../services/clinicService";

interface ClinicContextType {
  currentClinic: Clinic | null;
  clinics: Clinic[];
  isIndependent: boolean;
  loading: boolean;
  error: string | null;
  setCurrentClinic: (clinic: Clinic | null) => void;
  loadClinics: () => Promise<void>;
  refreshClinics: () => Promise<void>;
}

const ClinicContext = createContext<ClinicContextType | undefined>(undefined);

export function ClinicProvider({ children }: { children: React.ReactNode }) {
  const [currentClinic, setCurrentClinicState] = useState<Clinic | null>(null);
  const [clinics, setClinics] = useState<Clinic[]>([]);
  const [isIndependent, setIsIndependent] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadClinics = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const data = await clinicService.getClinics();
      setClinics(data);
      
      // Verificar se há clínica salva no localStorage
      const savedClinicId = localStorage.getItem("psipro_current_clinic_id");
      if (savedClinicId) {
        const saved = data.find((c) => c.id === savedClinicId);
        if (saved) {
          setCurrentClinicState(saved);
          setIsIndependent(false);
          setLoading(false);
          return;
        } else {
          // Clínica salva não existe mais, limpar
          localStorage.removeItem("psipro_current_clinic_id");
        }
      }
      
      setIsIndependent(data.length === 0);
    } catch (err: any) {
      console.error("Erro ao carregar clínicas:", err);
      setError(err.message || "Erro ao carregar clínicas");
      setClinics([]);
      setIsIndependent(true);
    } finally {
      setLoading(false);
    }
  };

  const refreshClinics = async () => {
    await loadClinics();
  };

  const setCurrentClinic = (clinic: Clinic | null) => {
    setCurrentClinicState(clinic);
    setIsIndependent(clinic === null);
    
    if (clinic) {
      localStorage.setItem("psipro_current_clinic_id", clinic.id);
    } else {
      localStorage.removeItem("psipro_current_clinic_id");
    }
  };

  useEffect(() => {
    loadClinics();
  }, []);

  return (
    <ClinicContext.Provider
      value={{
        currentClinic,
        clinics,
        isIndependent,
        loading,
        error,
        setCurrentClinic,
        loadClinics,
        refreshClinics,
      }}
    >
      {children}
    </ClinicContext.Provider>
  );
}

export function useClinic() {
  const context = useContext(ClinicContext);
  if (context === undefined) {
    throw new Error("useClinic must be used within a ClinicProvider");
  }
  return context;
}


