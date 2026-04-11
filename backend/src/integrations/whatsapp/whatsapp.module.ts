import { Module, forwardRef } from '@nestjs/common';
import { WhatsAppController } from './whatsapp.controller';
import { WhatsAppService } from './whatsapp.service';
import { SdrService } from './sdr.service';
import { PrismaModule } from '../../prisma/prisma.module';
import { AppointmentsModule } from '../../appointments/appointments.module';
import { PatientsModule } from '../../patients/patients.module';

@Module({
  imports: [
    PrismaModule,
    forwardRef(() => AppointmentsModule),
    forwardRef(() => PatientsModule),
  ],
  controllers: [WhatsAppController],
  providers: [
    WhatsAppService,
    {
      provide: 'SdrService',
      useClass: SdrService,
    },
    SdrService,
  ],
  exports: [WhatsAppService, SdrService],
})
export class WhatsAppModule {}
