/**
 * ETAPA 7 — Seed de migração segura para dados existentes
 *
 * Executar APÓS a migration add_multi_tenant_clinic_user_payment_subscription
 *
 * - Cria clinic "PSIPRO_DEFAULT"
 * - Associa todos usuários existentes sem clinicId a ela
 * - Atualiza financial_records e payments com clinicId do user
 *
 * Uso: npx ts-node prisma/migrate-existing-data.ts
 * ou:  npx tsx prisma/migrate-existing-data.ts
 */
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  console.log('🔄 Iniciando migração de dados para multi-tenant...');

  // 1. Criar ou obter clinic PSIPRO_DEFAULT
  let defaultClinic = await prisma.clinic.findFirst({
    where: { name: 'PSIPRO_DEFAULT' },
  });
  if (!defaultClinic) {
    defaultClinic = await prisma.clinic.create({
      data: {
        name: 'PSIPRO_DEFAULT',
        email: 'migracao@psipro.app',
        plan: 'basic',
        planType: 'CLINIC',
        status: 'active',
      },
    });
  }
  console.log('✅ Clinic PSIPRO_DEFAULT:', defaultClinic.id);

  // 2. Atualizar usuários sem clinicId
  const usersUpdated = await prisma.user.updateMany({
    where: { clinicId: null },
    data: {
      clinicId: defaultClinic.id,
      role: 'OWNER',
      isIndependent: true, // Mantém compatibilidade
    },
  });
  console.log(`✅ ${usersUpdated.count} usuários associados à PSIPRO_DEFAULT`);

  // 3. Atualizar financial_records: preencher clinicId a partir do userId
  const usersWithClinic = await prisma.user.findMany({
    where: { clinicId: { not: null } },
    select: { id: true, clinicId: true },
  });
  const userClinicMap = new Map(usersWithClinic.map((u) => [u.id, u.clinicId!]));

  const frs = await prisma.financialRecord.findMany({
    where: { clinicId: null },
    select: { id: true, userId: true },
  });
  let frUpdated = 0;
  for (const fr of frs) {
    const clinicId = userClinicMap.get(fr.userId) ?? defaultClinic.id;
    await prisma.financialRecord.update({
      where: { id: fr.id },
      data: { clinicId },
    });
    frUpdated++;
  }
  console.log(`✅ ${frUpdated} financial_records atualizados`);

  // 4. Atualizar payments: clinicId do patient ou user
  const payments = await prisma.payment.findMany({
    where: { clinicId: null },
    select: { id: true, userId: true, patientId: true },
  });
  const patients = await prisma.patient.findMany({
    where: { id: { in: [...new Set(payments.map((p) => p.patientId))] } },
    select: { id: true, clinicId: true, userId: true },
  });
  const patientMap = new Map(patients.map((p) => [p.id, p]));

  let payUpdated = 0;
  for (const pay of payments) {
    const patient = patientMap.get(pay.patientId);
    let clinicId =
      patient?.clinicId ?? userClinicMap.get(pay.userId) ?? defaultClinic.id;
    await prisma.payment.update({
      where: { id: pay.id },
      data: { clinicId },
    });
    payUpdated++;
  }
  console.log(`✅ ${payUpdated} payments atualizados`);

  console.log('🎉 Migração concluída com sucesso!');
}

main()
  .catch((e) => {
    console.error('❌ Erro na migração:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
