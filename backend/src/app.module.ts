import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { APP_GUARD } from '@nestjs/core';
import { ThrottlerGuard, ThrottlerModule } from '@nestjs/throttler';
import { ClinicContextGuard } from './common/guards/clinic-context.guard';
import { AuthModule } from './auth/auth.module';
import { ClinicsModule } from './clinics/clinics.module';
import { PatientsModule } from './patients/patients.module';
import { AppointmentsModule } from './appointments/appointments.module';
import { SessionsModule } from './sessions/sessions.module';
import { PaymentsModule } from './payments/payments.module';
import { FinancialModule } from './financial/financial.module';
import { DocumentsModule } from './documents/documents.module';
import { InsightsModule } from './insights/insights.module';
import { PrismaModule } from './prisma/prisma.module';
import { CommonModule } from './common/common.module';
import { SyncModule } from './sync/sync.module';
import { HealthModule } from './health/health.module';

/**
 * PsiPro Backend
 * - Fonte única de verdade (single source of truth) para identidade e dados.
 * - Android e Web consomem os mesmos contratos e endpoints.
 * - `GET /auth/me` é a base de identidade para clientes autenticados.
 */
@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    ThrottlerModule.forRoot([
      {
        ttl: 60,
        limit: 100,
      },
    ]),
    PrismaModule,
    CommonModule,
    HealthModule,
    AuthModule,
    ClinicsModule,
    PatientsModule,
    AppointmentsModule,
    SessionsModule,
    PaymentsModule,
    FinancialModule,
    DocumentsModule,
    InsightsModule,
    SyncModule,
  ],
  providers: [
    {
      provide: APP_GUARD,
      useClass: ThrottlerGuard,
    },
    ClinicContextGuard,
  ],
})
export class AppModule {}

