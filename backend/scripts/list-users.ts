/**
 * Lista usuários cadastrados no banco.
 * Uso: npx ts-node scripts/list-users.ts
 * (ou: npx ts-node -r tsconfig-paths/register scripts/list-users.ts)
 */
import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const users = await prisma.user.findMany({
    select: { id: true, email: true, name: true, createdAt: true },
    orderBy: { createdAt: 'desc' },
  });
  console.log(`\n=== ${users.length} usuário(s) cadastrado(s) ===\n`);
  users.forEach((u, i) => {
    console.log(`${i + 1}. ${u.email} (${u.name}) - id: ${u.id}`);
  });
  console.log('');
}

main()
  .catch(console.error)
  .finally(() => prisma.$disconnect());
