-- Soft delete: add deletedAt to Patient, Appointment, Session, Payment
ALTER TABLE "patients" ADD COLUMN "deletedAt" TIMESTAMP(3);
ALTER TABLE "appointments" ADD COLUMN "deletedAt" TIMESTAMP(3);
ALTER TABLE "sessions" ADD COLUMN "deletedAt" TIMESTAMP(3);
ALTER TABLE "payments" ADD COLUMN "deletedAt" TIMESTAMP(3);

-- FK adjustments: Session.appointmentId and Payment.sessionId set null on parent delete
-- (Prisma will generate these if schema diff requires)

-- Indexes for filtering soft-deleted
CREATE INDEX "patients_deletedAt_idx" ON "patients"("deletedAt");
CREATE INDEX "appointments_deletedAt_idx" ON "appointments"("deletedAt");
CREATE INDEX "sessions_deletedAt_idx" ON "sessions"("deletedAt");
CREATE INDEX "payments_deletedAt_idx" ON "payments"("deletedAt");
