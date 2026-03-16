-- AlterTable: adicionar ipAddress, userAgent, tornar clinicId NOT NULL (usar '' para registros existentes), FK para User
-- Primeiro: preencher clinicId nulos com valor default
UPDATE "audit_logs" SET "clinicId" = '' WHERE "clinicId" IS NULL;

-- AlterColumn: clinicId NOT NULL
ALTER TABLE "audit_logs" ALTER COLUMN "clinicId" SET NOT NULL;

-- AddColumn
ALTER TABLE "audit_logs" ADD COLUMN "ipAddress" TEXT;
ALTER TABLE "audit_logs" ADD COLUMN "userAgent" TEXT;

-- AddForeignKey (Prisma cria com nome específico)
ALTER TABLE "audit_logs" ADD CONSTRAINT "audit_logs_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- CreateIndex: índice composto clinicId + createdAt
CREATE INDEX "audit_logs_clinicId_createdAt_idx" ON "audit_logs"("clinicId", "createdAt");
