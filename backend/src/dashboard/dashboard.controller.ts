import { Controller, Get, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { DashboardService } from './dashboard.service';

@Controller('dashboard')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist', 'assistant')
export class DashboardController {
  constructor(private readonly dashboardService: DashboardService) {}

  @Get('metrics')
  async getMetrics(@CurrentClinicId() clinicId: string) {
    return this.dashboardService.getMetrics(clinicId);
  }

  @Get('agenda-summary')
  async getAgendaSummary(@CurrentClinicId() clinicId: string) {
    return this.dashboardService.getAgendaSummary(clinicId);
  }

  @Get('finance-summary')
  async getFinanceSummary(@CurrentClinicId() clinicId: string) {
    return this.dashboardService.getFinanceSummary(clinicId);
  }

  @Get('count')
  async getCount(@CurrentClinicId() clinicId: string) {
    return this.dashboardService.getCount(clinicId);
  }

  @Get('stats')
  async getStats(@CurrentClinicId() clinicId: string) {
    return this.dashboardService.getStats(clinicId);
  }

  @Get('summary')
  async getSummary(@CurrentClinicId() clinicId: string) {
    return this.dashboardService.getSummary(clinicId);
  }
}
