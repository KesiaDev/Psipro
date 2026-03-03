#!/usr/bin/env npx ts-node
/**
 * Script de limpeza segura — hard delete de registros soft-deleted antigos.
 *
 * Uso: npx ts-node scripts/cleanup-soft-deleted.ts [--days=90] [--dry-run]
 *
 * - --days=N: Remove registros com deletedAt anterior a N dias (default: 90)
 * - --dry-run: Apenas lista o que seria removido, não executa
 *
 * SEGURANÇA: Em produção, exige CONFIRM_CLEANUP=yes para executar.
 */

import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

const DAYS_DEFAULT = 90;

function parseArgs(): { days: number; dryRun: boolean } {
  const args = process.argv.slice(2);
  let days = DAYS_DEFAULT;
  let dryRun = false;
  for (const arg of args) {
    if (arg.startsWith('--days=')) days = parseInt(arg.split('=')[1], 10) || DAYS_DEFAULT;
    if (arg === '--dry-run') dryRun = true;
  }
  return { days, dryRun };
}

function cutoffDate(days: number): Date {
  const d = new Date();
  d.setDate(d.getDate() - days);
  return d;
}

async function main() {
  const { days, dryRun } = parseArgs();
  const cutoff = cutoffDate(days);

  const isProd = process.env.NODE_ENV === 'production';
  if (isProd && process.env.CONFIRM_CLEANUP !== 'yes' && !dryRun) {
    console.error('❌ Em produção, defina CONFIRM_CLEANUP=yes para executar.');
    process.exit(1);
  }

  console.log(`Cutoff: registros com deletedAt < ${cutoff.toISOString()} (${days} dias)`);
  if (dryRun) console.log('Modo: DRY-RUN (não remove)');

  const [patients, appointments, sessions, payments] = await Promise.all([
    prisma.patient.count({ where: { deletedAt: { lt: cutoff } } }),
    prisma.appointment.count({ where: { deletedAt: { lt: cutoff } } }),
    prisma.session.count({ where: { deletedAt: { lt: cutoff } } }),
    prisma.payment.count({ where: { deletedAt: { lt: cutoff } } }),
  ]);

  const total = patients + appointments + sessions + payments;
  console.log(`Encontrados: Patient=${patients}, Appointment=${appointments}, Session=${sessions}, Payment=${payments} (total=${total})`);

  if (total === 0) {
    console.log('Nada a remover.');
    return;
  }

  if (dryRun) {
    console.log('Dry-run: nenhuma remoção feita.');
    return;
  }

  // Ordem: Payment (referencia Session) -> Session (referencia Appointment) -> Appointment (referencia Patient) -> Patient
  // Com soft delete, FK ainda referenciam. Para hard delete precisamos respeitar ordem ou usar cascade.
  // O schema tem onDelete Cascade em Patient->Appointment, etc. Então deletar Patient primeiro cascateia?
  // Não - cascade é quando o PARENT é deletado. Aqui estamos deletando os filhos. Appointment referencia Patient.
  // Então: deletar Appointment primeiro (filhos de Patient), Session (referencia Appointment), Payment (referencia Session).
  const delPayment = prisma.payment.deleteMany({ where: { deletedAt: { lt: cutoff } } });
  const delSession = prisma.session.deleteMany({ where: { deletedAt: { lt: cutoff } } });
  const delAppointment = prisma.appointment.deleteMany({ where: { deletedAt: { lt: cutoff } } });
  const delPatient = prisma.patient.deleteMany({ where: { deletedAt: { lt: cutoff } } });

  const [rPay, rSess, rApt, rPat] = await Promise.all([delPayment, delSession, delAppointment, delPatient]);

  console.log(`Removidos: Patient=${rPat.count}, Appointment=${rApt.count}, Session=${rSess.count}, Payment=${rPay.count}`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
