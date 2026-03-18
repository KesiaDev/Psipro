-- CreateTable
CREATE TABLE "lgpd_consents" (
    "id" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "clinicId" TEXT NOT NULL,
    "patientId" TEXT,
    "type" TEXT NOT NULL,
    "version" TEXT NOT NULL DEFAULT '1.0',
    "accepted" BOOLEAN NOT NULL DEFAULT true,
    "ipAddress" TEXT,
    "userAgent" TEXT,
    "revokedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "lgpd_consents_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "lgpd_consents_userId_idx" ON "lgpd_consents"("userId");

-- CreateIndex
CREATE INDEX "lgpd_consents_clinicId_idx" ON "lgpd_consents"("clinicId");

-- CreateIndex
CREATE INDEX "lgpd_consents_patientId_idx" ON "lgpd_consents"("patientId");

-- CreateIndex
CREATE INDEX "lgpd_consents_clinicId_patientId_idx" ON "lgpd_consents"("clinicId", "patientId");
