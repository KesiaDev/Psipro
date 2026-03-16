import { Controller, Get, Patch, Param, UseGuards } from '@nestjs/common';
import { InsightsService } from './insights.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';

@Controller('insights')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class InsightsController {
  constructor(private readonly insightsService: InsightsService) {}

  @Get()
  findAll(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.insightsService.findAll(user.sub, clinicId);
  }

  /**
   * GET /insights/summary
   * Retorna no máximo 3 insights priorizados para consumo pelo App Android
   */
  @Get('summary')
  getSummary(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.insightsService.getSummary(user.sub, clinicId);
  }

  @Patch(':id/dismiss')
  dismiss(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.insightsService.dismiss(id, user.sub, clinicId);
  }
}



