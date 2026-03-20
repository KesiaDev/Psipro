-- AlterTable: sessions - add type and clinicalData (prontuário clínico)
ALTER TABLE "sessions" ADD COLUMN IF NOT EXISTS "type" TEXT;
ALTER TABLE "sessions" ADD COLUMN IF NOT EXISTS "clinicalData" JSONB;
