import { Module } from '@nestjs/common';
import { GoogleCalendarModule } from './google-calendar/google-calendar.module';
import { WhatsAppModule } from './whatsapp/whatsapp.module';

@Module({
  imports: [GoogleCalendarModule, WhatsAppModule],
  exports: [GoogleCalendarModule, WhatsAppModule],
})
export class IntegrationsModule {}
