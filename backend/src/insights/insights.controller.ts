import { Controller, Get, Patch, Param, UseGuards } from '@nestjs/common';
import { InsightsService } from './insights.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicContextGuard } from '../common/guards/clinic-context.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ClinicId } from '../common/decorators/clinic-id.decorator';

@Controller('insights')
@UseGuards(JwtAuthGuard, ClinicContextGuard)
export class InsightsController {
  constructor(private readonly insightsService: InsightsService) {}

  @Get()
  findAll(@CurrentUser() user: any, @ClinicId() clinicId: string | null) {
    return this.insightsService.findAll(user.sub, clinicId ?? undefined);
  }

  /**
   * GET /insights/summary
   * Retorna no máximo 3 insights priorizados para consumo pelo App Android
   */
  @Get('summary')
  getSummary(@CurrentUser() user: any, @ClinicId() clinicId: string | null) {
    return this.insightsService.getSummary(user.sub, clinicId ?? undefined);
  }

  @Patch(':id/dismiss')
  dismiss(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
  ) {
    return this.insightsService.dismiss(id, user.sub, clinicId ?? undefined);
  }
}



