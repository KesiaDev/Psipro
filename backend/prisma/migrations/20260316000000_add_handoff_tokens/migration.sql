-- CreateTable
CREATE TABLE "handoff_tokens" (
    "id" TEXT NOT NULL,
    "token" TEXT NOT NULL,
    "userId" TEXT NOT NULL,
    "clinicId" TEXT,
    "expiresAt" TIMESTAMP(3) NOT NULL,
    "usedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "handoff_tokens_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "handoff_tokens_token_key" ON "handoff_tokens"("token");

-- CreateIndex
CREATE INDEX "handoff_tokens_token_idx" ON "handoff_tokens"("token");

-- CreateIndex
CREATE INDEX "handoff_tokens_expiresAt_idx" ON "handoff_tokens"("expiresAt");

-- AddForeignKey
ALTER TABLE "handoff_tokens" ADD CONSTRAINT "handoff_tokens_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE CASCADE;
