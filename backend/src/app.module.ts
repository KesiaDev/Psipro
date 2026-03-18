import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { ScheduleModule } from '@nestjs/schedule';
import { LoggerModule } from './logger/logger.module';
import { AppThrottlerModule } from './throttler/throttler.module';
import { AuditModule } from './audit/audit.module';
import { CommonModule } from './common/common.module';
import { AuthModule } from './auth/auth.module';
import { ClinicsModule } from './clinics/clinics.module';
import { ProfessionalsModule } from './professionals/professionals.module';
import { UsersModule } from './users/users.module';
import { PatientsModule } from './patients/patients.module';
import { AppointmentsModule } from './appointments/appointments.module';
import { SessionsModule } from './sessions/sessions.module';
import { PaymentsModule } from './payments/payments.module';
import { FinancialModule } from './financial/financial.module';
import { DocumentsModule } from './documents/documents.module';
import { InsightsModule } from './insights/insights.module';
import { PrismaModule } from './prisma/prisma.module';
import { DashboardModule } from './dashboard/dashboard.module';
import { ReportsModule } from './reports/reports.module';
import { SyncModule } from './sync/sync.module';
import { AnamneseModule } from './anamnese/anamnese.module';
import { BetaModule } from './beta/beta.module';
import { VoiceModule } from './voice/voice.module';
import { IntegrationsModule } from './integrations/integrations.module';
import { SystemHealthModule } from './system-health/system-health.module';
import { HealthModule } from './health/health.module';

/**
 * PsiPro Backend
 * - Fonte única de verdade (single source of truth) para identidade e dados.
 * - Android e Web consomem os mesmos contratos e endpoints.
 * - `GET /auth/me` é a base de identidade para clientes autenticados.
 */
@Module({
  imports: [
    LoggerModule,
    AppThrottlerModule,
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    ScheduleModule.forRoot(),
    PrismaModule,
    AuditModule,
    CommonModule,
    AuthModule,
    ClinicsModule,
    ProfessionalsModule,
    UsersModule,
    PatientsModule,
    AppointmentsModule,
    SessionsModule,
    PaymentsModule,
    FinancialModule,
    DocumentsModule,
    InsightsModule,
    DashboardModule,
    ReportsModule,
    SyncModule,
    AnamneseModule,
    BetaModule,
    VoiceModule,
    IntegrationsModule,
    SystemHealthModule,
    HealthModule,
  ],
})
export class AppModule {}

