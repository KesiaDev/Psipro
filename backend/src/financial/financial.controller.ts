import { Controller, Get, Param, UseGuards } from '@nestjs/common';
import { FinancialService } from './financial.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('financial')
@UseGuards(JwtAuthGuard)
export class FinancialController {
  constructor(private readonly financialService: FinancialService) {}

  @Get('summary')
  getSummary(@CurrentUser() user: any) {
    return this.financialService.getSummary(user.sub);
  }

  @Get('patient/:patientId')
  getPatientFinancial(@Param('patientId') patientId: string, @CurrentUser() user: any) {
    return this.financialService.getPatientFinancial(patientId, user.sub);
  }
}




