-- AlterTable
ALTER TABLE "appointments" ADD COLUMN "reminder12hSentAt" TIMESTAMP(3),
                           ADD COLUMN "reminder2hSentAt"  TIMESTAMP(3);
