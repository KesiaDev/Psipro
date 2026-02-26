import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicContextGuard } from '../common/guards/clinic-context.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ClinicId } from '../common/decorators/clinic-id.decorator';
import { SyncPatientsBodyDto } from './dto/sync-patients-body.dto';
import { SyncPatientsQueryDto } from './dto/sync-patients-query.dto';
import { SyncService } from './sync.service';

@Controller('sync')
@UseGuards(JwtAuthGuard, ClinicContextGuard)
export class SyncController {
  constructor(private readonly syncService: SyncService) {}

  /**
   * GET /sync/patients
   * Retorna pacientes da clínica para sincronização bidirecional Android <-> Web.
   * X-Clinic-Id: obrigatório quando usuário pertence a múltiplas clínicas.
   */
  @Get('patients')
  getPatients(
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
    @Query() query: SyncPatientsQueryDto,
  ) {
    const effectiveQuery = {
      ...query,
      clinicId: clinicId ?? query.clinicId,
    };
    return this.syncService.getPatients(user.sub, effectiveQuery);
  }

  /**
   * POST /sync/patients
   * Recebe lista do App e resolve conflitos no backend (single source of truth).
   */
  @Post('patients')
  syncPatients(
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
    @Query() query: SyncPatientsQueryDto,
    @Body() body: SyncPatientsBodyDto,
  ) {
    const effectiveQuery = {
      ...query,
      clinicId: clinicId ?? query.clinicId,
    };
    return this.syncService.syncPatients(user.sub, effectiveQuery, body.patients);
  }
}

