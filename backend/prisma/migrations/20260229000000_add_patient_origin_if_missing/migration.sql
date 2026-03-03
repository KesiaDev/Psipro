-- Add PatientOrigin enum if not exists (production may have schema without it)
DO $$ BEGIN
    CREATE TYPE "PatientOrigin" AS ENUM ('ANDROID', 'WEB');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- Add origin column to patients if not exists
ALTER TABLE "patients" ADD COLUMN IF NOT EXISTS "origin" "PatientOrigin" NOT NULL DEFAULT 'WEB';
