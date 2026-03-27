-- CreateTable: intake_tokens - token público de intake de paciente (LGPD Art. 11)
CREATE TABLE "intake_tokens" (
    "id" TEXT NOT NULL,
    "token" TEXT NOT NULL,
    "clinicId" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "expiresAt" TIMESTAMP(3) NOT NULL,
    "usedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "intake_tokens_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "intake_tokens_token_key" ON "intake_tokens"("token");
CREATE INDEX "intake_tokens_token_idx" ON "intake_tokens"("token");
CREATE INDEX "intake_tokens_clinicId_idx" ON "intake_tokens"("clinicId");
CREATE INDEX "intake_tokens_expiresAt_idx" ON "intake_tokens"("expiresAt");

-- AddForeignKey
ALTER TABLE "intake_tokens" ADD CONSTRAINT "intake_tokens_clinicId_fkey"
    FOREIGN KEY ("clinicId") REFERENCES "clinics"("id") ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE "intake_tokens" ADD CONSTRAINT "intake_tokens_userId_fkey"
    FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
