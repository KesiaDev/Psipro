import { PrismaClient } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Seeding database...');

  const hashedPassword = await bcrypt.hash('senha123', 10);

  // Criar usuário owner (dono da clínica)
  const owner = await prisma.user.upsert({
    where: { email: 'owner@psiclinic.com' },
    update: {},
    create: {
      email: 'owner@psiclinic.com',
      name: 'Dr. Carlos Mendes',
      password: hashedPassword,
      isIndependent: false,
      license: 'CRP 06/123456',
    },
  });

  // Criar psicólogo independente
  const independent = await prisma.user.upsert({
    where: { email: 'psicologo@psipro.com' },
    update: {},
    create: {
      email: 'psicologo@psipro.com',
      name: 'Psicólogo Exemplo',
      password: hashedPassword,
      isIndependent: true,
    },
  });

  // Criar outro psicólogo para a clínica
  const psychologist = await prisma.user.upsert({
    where: { email: 'psicologo2@psiclinic.com' },
    update: {},
    create: {
      email: 'psicologo2@psiclinic.com',
      name: 'Dra. Ana Paula Silva',
      password: hashedPassword,
      isIndependent: false,
      license: 'CRP 06/789012',
    },
  });

  console.log('✅ Usuários criados');

  // Criar clínica
  const clinic = await prisma.clinic.upsert({
    where: { id: 'clinic-1' },
    update: { planType: 'CLINIC' },
    create: {
      id: 'clinic-1',
      name: 'PsiClinic - Centro de Psicologia',
      cnpj: '12.345.678/0001-90',
      email: 'contato@psiclinic.com',
      phone: '(11) 3456-7890',
      address: 'Rua Exemplo, 123 - São Paulo, SP',
      plan: 'professional',
      planType: 'CLINIC',
      status: 'active',
    },
  });

  // Atualizar owner e psychologist com clinicId
  await prisma.user.update({
    where: { id: owner.id },
    data: { clinicId: clinic.id, role: 'OWNER', isIndependent: false },
  });
  await prisma.user.update({
    where: { id: psychologist.id },
    data: { clinicId: clinic.id, role: 'PSYCHOLOGIST', isIndependent: false },
  });

  // Psicólogo independente: criar sua própria clinic INDIVIDUAL
  const individualClinic = await prisma.clinic.create({
    data: {
      name: independent.name,
      email: independent.email,
      plan: 'basic',
      planType: 'INDIVIDUAL',
      status: 'active',
    },
  });
  await prisma.user.update({
    where: { id: independent.id },
    data: { clinicId: individualClinic.id, role: 'OWNER', isIndependent: true },
  });

  // Adicionar owner à clínica (ClinicUser)
  await prisma.clinicUser.upsert({
    where: {
      clinicId_userId: {
        clinicId: clinic.id,
        userId: owner.id,
      },
    },
    update: {},
    create: {
      clinicId: clinic.id,
      userId: owner.id,
      role: 'owner',
      status: 'active',
      canViewAllPatients: true,
      canEditAllPatients: true,
      canViewFinancial: true,
      canManageUsers: true,
    },
  });

  // Adicionar psicólogo à clínica
  await prisma.clinicUser.upsert({
    where: {
      clinicId_userId: {
        clinicId: clinic.id,
        userId: psychologist.id,
      },
    },
    update: {},
    create: {
      clinicId: clinic.id,
      userId: psychologist.id,
      role: 'psychologist',
      status: 'active',
      canViewAllPatients: false,
      canEditAllPatients: false,
      canViewFinancial: false,
      canManageUsers: false,
    },
  });

  console.log('✅ Clínica criada:', clinic.name);

  // Criar pacientes da clínica
  const clinicPatients = await Promise.all([
    prisma.patient.upsert({
      where: { id: 'clinic-patient-1' },
      update: {},
      create: {
        id: 'clinic-patient-1',
        clinicId: clinic.id,
        clinicOwnerId: owner.id,
        name: 'Maria Silva Santos',
        phone: '(11) 98765-4321',
        email: 'maria.silva@email.com',
        status: 'Ativo',
        type: 'Adulto',
        source: 'web',
        sharedWith: [psychologist.id], // Compartilhado com psicólogo
      },
    }),
    prisma.patient.upsert({
      where: { id: 'clinic-patient-2' },
      update: {},
      create: {
        id: 'clinic-patient-2',
        clinicId: clinic.id,
        clinicOwnerId: owner.id,
        name: 'João Pedro Oliveira',
        phone: '(11) 97654-3210',
        email: 'joao.oliveira@email.com',
        status: 'Ativo',
        type: 'Adulto',
        source: 'web',
      },
    }),
  ]);

  // Criar pacientes do psicólogo independente
  const independentPatients = await Promise.all([
    prisma.patient.upsert({
      where: { id: 'patient-1' },
      update: {},
      create: {
        id: 'patient-1',
        userId: independent.id,
        name: 'Roberto Alves',
        phone: '(11) 91234-5678',
        email: 'roberto@email.com',
        status: 'Ativo',
        type: 'Adulto',
        source: 'web',
      },
    }),
  ]);

  console.log('✅ Pacientes criados:', clinicPatients.length + independentPatients.length);

  // Registros financeiros de teste (apenas se não existirem)
  const financialCount = await prisma.financialRecord.count();
  if (financialCount === 0) {
    await prisma.financialRecord.createMany({
      data: [
        {
          userId: owner.id,
          clinicId: clinic.id,
          date: new Date(),
          type: 'receita',
          amount: 150.0,
          description: 'Sessão - Maria Silva',
          category: 'session',
        },
        {
          userId: owner.id,
          clinicId: clinic.id,
          date: new Date(),
          type: 'despesa',
          amount: 50.0,
          description: 'Material de expediente',
          category: 'other',
        },
      ],
    });
    console.log('✅ 2 registros financeiros de teste criados');
  }

  console.log('🎉 Seed concluído!');
  console.log('\n📋 Credenciais:');
  console.log('  Owner da Clínica: owner@psiclinic.com / senha123');
  console.log('  Psicólogo Clínica: psicologo2@psiclinic.com / senha123');
  console.log('  Psicólogo Independente: psicologo@psipro.com / senha123');
}

main()
  .catch((e) => {
    console.error('❌ Erro no seed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });

