import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { SyncPatientsBodyDto } from './dto/sync-patients-body.dto';
import { SyncPatientsQueryDto } from './dto/sync-patients-query.dto';
import { SyncService } from './sync.service';

@Controller('sync')
@UseGuards(JwtAuthGuard, ClinicGuard)
export class SyncController {
  constructor(private readonly syncService: SyncService) {}

  /**
   * GET /sync/patients
   * Retorna pacientes da clínica para sincronização bidirecional Android <-> Web.
   * Usa @CurrentClinicId() para multi-tenant.
   */
  @Get('patients')
  getPatients(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Query() query: SyncPatientsQueryDto,
  ) {
    return this.syncService.getPatients(user.sub, clinicId, query);
  }

  /**
   * POST /sync/patients
   * Recebe lista do App e resolve conflitos no backend (single source of truth).
   */
  @Post('patients')
  syncPatients(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Query() query: SyncPatientsQueryDto,
    @Body() body: SyncPatientsBodyDto,
  ) {
    return this.syncService.syncPatients(user.sub, clinicId, body.patients);
  }
}

