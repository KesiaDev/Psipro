import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { SyncSessionsBodyDto } from './dto/sync-sessions-body.dto';
import { SyncAppointmentsQueryDto } from './dto/sync-appointments-query.dto';
import { SyncSessionsService } from './sync-sessions.service';

@Controller('sync/sessions')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class SyncSessionsController {
  constructor(private readonly syncSessionsService: SyncSessionsService) {}

  @Get()
  getSessions(
    @CurrentUser() _user: any,
    @CurrentClinicId() clinicId: string,
    @Query() query: SyncAppointmentsQueryDto,
  ) {
    return this.syncSessionsService.getSessions(clinicId, query.updatedAfter);
  }

  @Post()
  syncSessions(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() body: SyncSessionsBodyDto,
  ) {
    return this.syncSessionsService.syncSessions(user.sub, clinicId, body.sessions);
  }
}
