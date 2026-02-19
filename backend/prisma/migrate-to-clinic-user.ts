/**
 * Migração: Para cada User com clinicId, cria ClinicUser com role OWNER.
 * Não apaga dados antigos.
 * Uso: npx ts-node prisma/migrate-to-clinic-user.ts
 */
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const usersWithClinic = await prisma.user.findMany({
    where: { clinicId: { not: null } },
    select: { id: true, clinicId: true },
  });

  console.log(`Encontrados ${usersWithClinic.length} usuários com clinicId`);

  for (const u of usersWithClinic) {
    if (!u.clinicId) continue;
    const existing = await prisma.clinicUser.findUnique({
      where: {
        clinicId_userId: { clinicId: u.clinicId, userId: u.id },
      },
    });
    if (existing) {
      console.log(`  ClinicUser já existe: user=${u.id} clinic=${u.clinicId}`);
      continue;
    }
    await prisma.clinicUser.create({
      data: {
        clinicId: u.clinicId,
        userId: u.id,
        role: 'owner',
        status: 'active',
        canViewAllPatients: true,
        canEditAllPatients: true,
        canViewFinancial: true,
        canManageUsers: true,
      },
    });
    console.log(`  Criado ClinicUser: user=${u.id} clinic=${u.clinicId}`);
  }

  console.log('Migração concluída.');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
