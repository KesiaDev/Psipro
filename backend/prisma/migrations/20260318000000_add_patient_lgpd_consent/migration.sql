-- AlterTable
-- LGPD: consentimento do paciente (compatível com dados existentes)
-- consentGiven: default false para pacientes já cadastrados (não quebra queries)
-- consentAt: nullable para pacientes sem registro de aceite
ALTER TABLE "patients" ADD COLUMN "consentGiven" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "patients" ADD COLUMN "consentAt" TIMESTAMP(3);
