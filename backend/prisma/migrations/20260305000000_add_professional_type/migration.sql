-- CreateEnum
CREATE TYPE "ProfessionalType" AS ENUM ('psychologist', 'therapist', 'psychoanalyst', 'counselor', 'coach', 'other');

-- AlterTable
ALTER TABLE "users" ADD COLUMN "professionalType" "ProfessionalType";
