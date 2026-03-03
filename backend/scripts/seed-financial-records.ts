/**
 * Script para verificar/criar registros na tabela financial_records.
 * Uso: npm run prisma:seed-financial  OU  npx ts-node scripts/seed-financial-records.ts
 */
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const count = await prisma.financialRecord.count();
  console.log(`📊 Registros em financial_records: ${count}`);

  if (count > 0) {
    console.log('✅ Tabela já possui registros. Nada a fazer.');
    return;
  }

  // Buscar um usuário e clínica existentes
  const user = await prisma.user.findFirst();
  const clinic = await prisma.clinic.findFirst();

  if (!user) {
    console.error('❌ Nenhum usuário encontrado. Rode o seed principal primeiro: npm run prisma:seed');
    process.exit(1);
  }

  const clinicId = clinic?.id ?? null;
  if (!clinicId) {
    console.log('⚠️ Nenhuma clínica encontrada. Criando registros com clinicId=null.');
  }

  await prisma.financialRecord.createMany({
    data: [
      { userId: user.id, clinicId, date: new Date(), type: 'receita', amount: 150, description: 'Sessão - teste', category: 'session' },
      { userId: user.id, clinicId, date: new Date(), type: 'despesa', amount: 50, description: 'Material de expediente', category: 'other' },
    ],
  });

  console.log('✅ 2 registros financeiros de teste criados.');
}

main()
  .catch((e) => {
    console.error('❌ Erro:', e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
