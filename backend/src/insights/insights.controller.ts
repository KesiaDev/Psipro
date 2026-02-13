import { Controller, Get, Patch, Param, UseGuards } from '@nestjs/common';
import { InsightsService } from './insights.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('insights')
@UseGuards(JwtAuthGuard)
export class InsightsController {
  constructor(private readonly insightsService: InsightsService) {}

  @Get()
  findAll(@CurrentUser() user: any) {
    return this.insightsService.findAll(user.sub);
  }

  /**
   * GET /insights/summary
   * Retorna no máximo 3 insights priorizados para consumo pelo App Android
   */
  @Get('summary')
  getSummary(@CurrentUser() user: any) {
    return this.insightsService.getSummary(user.sub);
  }

  @Patch(':id/dismiss')
  dismiss(@Param('id') id: string, @CurrentUser() user: any) {
    return this.insightsService.dismiss(id, user.sub);
  }
}



