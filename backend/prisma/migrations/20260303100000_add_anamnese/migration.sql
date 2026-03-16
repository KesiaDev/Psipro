-- CreateTable
CREATE TABLE "anamnese_models" (
    "id" TEXT NOT NULL,
    "clinicId" TEXT,
    "userId" TEXT,
    "nome" TEXT NOT NULL,
    "isDefault" BOOLEAN NOT NULL DEFAULT false,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "anamnese_models_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "anamnese_campos" (
    "id" TEXT NOT NULL,
    "modeloId" TEXT NOT NULL,
    "tipo" TEXT NOT NULL,
    "label" TEXT NOT NULL,
    "opcoes" TEXT,
    "obrigatorio" BOOLEAN NOT NULL DEFAULT false,
    "ordem" INTEGER NOT NULL DEFAULT 0,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "anamnese_campos_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "anamnese_preenchidas" (
    "id" TEXT NOT NULL,
    "patientId" TEXT NOT NULL,
    "modeloId" TEXT NOT NULL,
    "respostas" JSONB NOT NULL,
    "assinaturaPath" TEXT,
    "data" TIMESTAMP(3) NOT NULL,
    "versao" INTEGER NOT NULL DEFAULT 1,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "anamnese_preenchidas_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE INDEX "anamnese_models_clinicId_idx" ON "anamnese_models"("clinicId");

-- CreateIndex
CREATE INDEX "anamnese_models_userId_idx" ON "anamnese_models"("userId");

-- CreateIndex
CREATE INDEX "anamnese_campos_modeloId_idx" ON "anamnese_campos"("modeloId");

-- CreateIndex
CREATE INDEX "anamnese_preenchidas_patientId_idx" ON "anamnese_preenchidas"("patientId");

-- CreateIndex
CREATE INDEX "anamnese_preenchidas_modeloId_idx" ON "anamnese_preenchidas"("modeloId");

-- AddForeignKey
ALTER TABLE "anamnese_models" ADD CONSTRAINT "anamnese_models_clinicId_fkey" FOREIGN KEY ("clinicId") REFERENCES "clinics"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "anamnese_campos" ADD CONSTRAINT "anamnese_campos_modeloId_fkey" FOREIGN KEY ("modeloId") REFERENCES "anamnese_models"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "anamnese_preenchidas" ADD CONSTRAINT "anamnese_preenchidas_patientId_fkey" FOREIGN KEY ("patientId") REFERENCES "patients"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "anamnese_preenchidas" ADD CONSTRAINT "anamnese_preenchidas_modeloId_fkey" FOREIGN KEY ("modeloId") REFERENCES "anamnese_models"("id") ON DELETE CASCADE ON UPDATE CASCADE;
