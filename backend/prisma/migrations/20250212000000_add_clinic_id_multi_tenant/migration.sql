-- AlterTable
ALTER TABLE "sessions" ADD COLUMN IF NOT EXISTS "clinicId" TEXT;

-- AlterTable
ALTER TABLE "payments" ADD COLUMN IF NOT EXISTS "clinicId" TEXT;

-- AlterTable
ALTER TABLE "financial_records" ADD COLUMN IF NOT EXISTS "clinicId" TEXT;

-- AlterTable
ALTER TABLE "documents" ADD COLUMN IF NOT EXISTS "clinicId" TEXT;

-- AlterTable
ALTER TABLE "insights" ADD COLUMN IF NOT EXISTS "clinicId" TEXT;

-- CreateIndex
CREATE INDEX IF NOT EXISTS "sessions_clinicId_idx" ON "sessions"("clinicId");

-- CreateIndex
CREATE INDEX IF NOT EXISTS "payments_clinicId_idx" ON "payments"("clinicId");

-- CreateIndex
CREATE INDEX IF NOT EXISTS "financial_records_clinicId_idx" ON "financial_records"("clinicId");

-- CreateIndex
CREATE INDEX IF NOT EXISTS "documents_clinicId_idx" ON "documents"("clinicId");

-- CreateIndex
CREATE INDEX IF NOT EXISTS "insights_clinicId_idx" ON "insights"("clinicId");
