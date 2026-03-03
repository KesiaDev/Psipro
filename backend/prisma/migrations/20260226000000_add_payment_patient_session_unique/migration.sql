-- CreateIndex: Garantir unicidade (patientId, sessionNumber) para evitar duplicação sob concorrência
CREATE UNIQUE INDEX "payments_patientId_sessionNumber_key" ON "payments"("patientId", "sessionNumber");
