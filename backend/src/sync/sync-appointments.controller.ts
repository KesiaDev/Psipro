import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { SyncAppointmentsBodyDto } from './dto/sync-appointments-body.dto';
import { SyncAppointmentsQueryDto } from './dto/sync-appointments-query.dto';
import { SyncAppointmentsService } from './sync-appointments.service';

@Controller('sync/appointments')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class SyncAppointmentsController {
  constructor(private readonly syncAppointmentsService: SyncAppointmentsService) {}

  /**
   * GET /api/sync/appointments
   * Retorna agendamentos da clínica para sincronização.
   * Query: clinicId (via x-clinic-id), updatedAfter (opcional)
   */
  @Get()
  getAppointments(
    @CurrentUser() _user: any,
    @CurrentClinicId() clinicId: string,
    @Query() query: SyncAppointmentsQueryDto,
  ) {
    return this.syncAppointmentsService.getAppointments(clinicId, query.updatedAfter);
  }

  /**
   * POST /api/sync/appointments
   * Recebe lista do app e resolve conflitos no backend (source of truth).
   * Registros com updatedAt mais recente vencem.
   * Retorna lista atualizada após sync.
   */
  @Post()
  syncAppointments(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() body: SyncAppointmentsBodyDto,
  ) {
    return this.syncAppointmentsService.syncAppointments(user.sub, clinicId, body.appointments);
  }
}
