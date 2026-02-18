import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
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
import { DashboardModule } from './dashboard/dashboard.module';
import { SyncModule } from './sync/sync.module';

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
    PrismaModule,
    AuthModule,
    ClinicsModule,
    PatientsModule,
    AppointmentsModule,
    SessionsModule,
    PaymentsModule,
    FinancialModule,
    DocumentsModule,
    InsightsModule,
    DashboardModule,
    SyncModule,
  ],
})
export class AppModule {}

