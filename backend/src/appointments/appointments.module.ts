import { Module, forwardRef } from '@nestjs/common';
import { AppointmentsController } from './appointments.controller';
import { AppointmentsService } from './appointments.service';
import { GoogleCalendarModule } from '../integrations/google-calendar/google-calendar.module';
import { WhatsAppModule } from '../integrations/whatsapp/whatsapp.module';

@Module({
  imports: [GoogleCalendarModule, forwardRef(() => WhatsAppModule)],
  controllers: [AppointmentsController],
  providers: [AppointmentsService],
  exports: [AppointmentsService],
})
export class AppointmentsModule {}




