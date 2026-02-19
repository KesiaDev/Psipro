import { Controller, Get, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { DashboardService } from './dashboard.service';

@Controller('dashboard')
@UseGuards(JwtAuthGuard, ClinicGuard)
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
}
