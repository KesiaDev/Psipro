import { PrismaClient } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Seeding database...');

  // Criar usuário de exemplo
  const hashedPassword = await bcrypt.hash('senha123', 10);

  const user = await prisma.user.upsert({
    where: { email: 'psicologo@psipro.com' },
    update: {},
    create: {
      email: 'psicologo@psipro.com',
      name: 'Psicólogo Exemplo',
      password: hashedPassword,
    },
  });

  console.log('✅ Usuário criado:', user.email);

  // Criar alguns pacientes de exemplo
  const patients = await Promise.all([
    prisma.patient.upsert({
      where: { id: 'patient-1' },
      update: {},
      create: {
        id: 'patient-1',
        userId: user.id,
        name: 'Maria Silva Santos',
        phone: '(11) 98765-4321',
        email: 'maria.silva@email.com',
        status: 'Ativo',
        type: 'Adulto',
        source: 'web',
      },
    }),
    prisma.patient.upsert({
      where: { id: 'patient-2' },
      update: {},
      create: {
        id: 'patient-2',
        userId: user.id,
        name: 'João Pedro Oliveira',
        phone: '(11) 97654-3210',
        email: 'joao.oliveira@email.com',
        status: 'Ativo',
        type: 'Adulto',
        source: 'web',
      },
    }),
  ]);

  console.log('✅ Pacientes criados:', patients.length);

  console.log('🎉 Seed concluído!');
}

main()
  .catch((e) => {
    console.error('❌ Erro no seed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });

