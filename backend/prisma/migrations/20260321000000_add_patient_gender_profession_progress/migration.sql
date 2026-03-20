-- AlterTable: patients - add gender, profession, progress (ficha de acolhimento e evolução clínica)
ALTER TABLE "patients" ADD COLUMN IF NOT EXISTS "gender" TEXT;
ALTER TABLE "patients" ADD COLUMN IF NOT EXISTS "profession" TEXT;
ALTER TABLE "patients" ADD COLUMN IF NOT EXISTS "progress" TEXT;
