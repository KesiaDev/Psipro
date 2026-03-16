import { Module } from '@nestjs/common';
import { SyncController } from './sync.controller';
import { SyncAppointmentsController } from './sync-appointments.controller';
import { SyncSessionsController } from './sync-sessions.controller';
import { SyncPaymentsController } from './sync-payments.controller';
import { SyncDocumentsController } from './sync-documents.controller';
import { SyncService } from './sync.service';
import { SyncAppointmentsService } from './sync-appointments.service';
import { SyncSessionsService } from './sync-sessions.service';
import { SyncPaymentsService } from './sync-payments.service';
import { SyncDocumentsService } from './sync-documents.service';

@Module({
  controllers: [
    SyncController,
    SyncAppointmentsController,
    SyncSessionsController,
    SyncPaymentsController,
    SyncDocumentsController,
  ],
  providers: [
    SyncService,
    SyncAppointmentsService,
    SyncSessionsService,
    SyncPaymentsService,
    SyncDocumentsService,
  ],
})
export class SyncModule {}

