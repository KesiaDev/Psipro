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

  @Patch(':id/dismiss')
  dismiss(@Param('id') id: string, @CurrentUser() user: any) {
    return this.insightsService.dismiss(id, user.sub);
  }
}

