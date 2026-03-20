-- AlterTable: financial_records - add sessionId, patientId, status, paymentMethod
ALTER TABLE "financial_records" ADD COLUMN IF NOT EXISTS "patientId" TEXT;
ALTER TABLE "financial_records" ADD COLUMN IF NOT EXISTS "sessionId" TEXT;
ALTER TABLE "financial_records" ADD COLUMN IF NOT EXISTS "status" TEXT DEFAULT 'pendente';
ALTER TABLE "financial_records" ADD COLUMN IF NOT EXISTS "paymentMethod" TEXT;

-- CreateIndex
CREATE UNIQUE INDEX IF NOT EXISTS "financial_records_sessionId_key" ON "financial_records"("sessionId");
CREATE INDEX IF NOT EXISTS "financial_records_patientId_idx" ON "financial_records"("patientId");
CREATE INDEX IF NOT EXISTS "financial_records_sessionId_idx" ON "financial_records"("sessionId");
CREATE INDEX IF NOT EXISTS "financial_records_status_idx" ON "financial_records"("status");

-- AddForeignKey
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_name = 'financial_records_patientId_fkey'
  ) THEN
    ALTER TABLE "financial_records" ADD CONSTRAINT "financial_records_patientId_fkey"
      FOREIGN KEY ("patientId") REFERENCES "patients"("id") ON DELETE SET NULL ON UPDATE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints
    WHERE constraint_name = 'financial_records_sessionId_fkey'
  ) THEN
    ALTER TABLE "financial_records" ADD CONSTRAINT "financial_records_sessionId_fkey"
      FOREIGN KEY ("sessionId") REFERENCES "sessions"("id") ON DELETE SET NULL ON UPDATE CASCADE;
  END IF;
END $$;
