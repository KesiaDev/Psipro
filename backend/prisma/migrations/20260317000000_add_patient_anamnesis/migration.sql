-- AlterTable
ALTER TABLE "patients" ADD COLUMN IF NOT EXISTS "anamnesis" JSONB;
