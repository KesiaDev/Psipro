-- CreateEnum
CREATE TYPE "PlanType" AS ENUM ('INDIVIDUAL', 'CLINIC');

-- CreateEnum
CREATE TYPE "UserRole" AS ENUM ('OWNER', 'PSYCHOLOGIST', 'ASSISTANT');

-- CreateEnum
CREATE TYPE "SubscriptionStatus" AS ENUM ('ACTIVE', 'PAST_DUE', 'CANCELED');

-- AlterTable: clinics - add planType
ALTER TABLE "clinics" ADD COLUMN IF NOT EXISTS "planType" "PlanType" NOT NULL DEFAULT 'CLINIC';

-- AlterTable: users - add clinicId and role
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS "clinicId" TEXT;
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS "role" "UserRole" NOT NULL DEFAULT 'OWNER';

-- AlterTable: financial_records - add clinicId
ALTER TABLE "financial_records" ADD COLUMN IF NOT EXISTS "clinicId" TEXT;

-- AlterTable: payments - add clinicId
ALTER TABLE "payments" ADD COLUMN IF NOT EXISTS "clinicId" TEXT;

-- CreateTable: subscriptions
CREATE TABLE IF NOT EXISTS "subscriptions" (
    "id" TEXT NOT NULL,
    "clinicId" TEXT NOT NULL,
    "status" "SubscriptionStatus" NOT NULL DEFAULT 'ACTIVE',
    "stripeCustomerId" TEXT,
    "stripeSubscriptionId" TEXT,
    "currentPeriodEnd" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "subscriptions_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX IF NOT EXISTS "subscriptions_clinicId_idx" ON "subscriptions"("clinicId");
CREATE INDEX IF NOT EXISTS "users_clinicId_idx" ON "users"("clinicId");
CREATE INDEX IF NOT EXISTS "financial_records_clinicId_idx" ON "financial_records"("clinicId");
CREATE INDEX IF NOT EXISTS "payments_clinicId_idx" ON "payments"("clinicId");

-- AddForeignKey (após garantir que clinics existem)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints 
    WHERE constraint_name = 'users_clinicId_fkey'
  ) THEN
    ALTER TABLE "users" ADD CONSTRAINT "users_clinicId_fkey" 
    FOREIGN KEY ("clinicId") REFERENCES "clinics"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints 
    WHERE constraint_name = 'financial_records_clinicId_fkey'
  ) THEN
    ALTER TABLE "financial_records" ADD CONSTRAINT "financial_records_clinicId_fkey" 
    FOREIGN KEY ("clinicId") REFERENCES "clinics"("id") ON DELETE SET NULL ON UPDATE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints 
    WHERE constraint_name = 'payments_clinicId_fkey'
  ) THEN
    ALTER TABLE "payments" ADD CONSTRAINT "payments_clinicId_fkey" 
    FOREIGN KEY ("clinicId") REFERENCES "clinics"("id") ON DELETE SET NULL ON UPDATE CASCADE;
  END IF;
END $$;

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints 
    WHERE constraint_name = 'subscriptions_clinicId_fkey'
  ) THEN
    ALTER TABLE "subscriptions" ADD CONSTRAINT "subscriptions_clinicId_fkey" 
    FOREIGN KEY ("clinicId") REFERENCES "clinics"("id") ON DELETE CASCADE ON UPDATE CASCADE;
  END IF;
END $$;
