import { Controller, Get, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ReportsService } from './reports.service';

@Controller('reports')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class ReportsController {
  constructor(private readonly reportsService: ReportsService) {}

  @Get()
  findAll(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.reportsService.findAll(user.sub, clinicId);
  }

  @Get('summary')
  getSummary(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.reportsService.getSummary(user.sub, clinicId);
  }

  @Get('today')
  getToday(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.reportsService.getToday(user.sub, clinicId);
  }

  @Get('stats')
  getStats(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.reportsService.getStats(user.sub, clinicId);
  }

  @Get('count')
  getCount(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.reportsService.getCount(user.sub, clinicId);
  }
}
