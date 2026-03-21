/**
 * Script para excluir a clínica "Claudia Cruz" (mantém "Terapeuta Claudia Cruz").
 * Uso: npx ts-node scripts/delete-clinic-by-name.ts
 *
 * Requer DATABASE_URL no .env
 */
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

const CLINIC_TO_DELETE = 'Claudia Cruz'; // Nome exato da clínica a excluir

async function main() {
  const clinic = await prisma.clinic.findFirst({
    where: { name: CLINIC_TO_DELETE },
  });

  if (!clinic) {
    console.log(`⚠️ Clínica "${CLINIC_TO_DELETE}" não encontrada. Nada a fazer.`);
    return;
  }

  // Listar clínicas para confirmar qual será removida
  const allClinics = await prisma.clinic.findMany({
    select: { id: true, name: true },
  });
  console.log('Clínicas encontradas:', allClinics.map((c) => c.name));

  // Remover clinicId de users que usam esta clínica como principal
  const usersUpdated = await prisma.user.updateMany({
    where: { clinicId: clinic.id },
    data: { clinicId: null },
  });
  if (usersUpdated.count > 0) {
    console.log(`✅ ${usersUpdated.count} usuário(s) atualizado(s) (clinicId removido)`);
  }

  // Deletar clínica (cascata remove ClinicUser, Patient, etc.)
  await prisma.clinic.delete({
    where: { id: clinic.id },
  });

  console.log(`✅ Clínica "${CLINIC_TO_DELETE}" (id: ${clinic.id}) excluída com sucesso.`);
}

main()
  .catch((e) => {
    console.error('❌ Erro:', e.message);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
