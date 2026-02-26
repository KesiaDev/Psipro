import { Controller, Get, Param, UseGuards } from '@nestjs/common';
import { FinancialService } from './financial.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicContextGuard } from '../common/guards/clinic-context.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ClinicId } from '../common/decorators/clinic-id.decorator';

@Controller('financial')
@UseGuards(JwtAuthGuard, ClinicContextGuard)
export class FinancialController {
  constructor(private readonly financialService: FinancialService) {}

  @Get('summary')
  getSummary(@CurrentUser() user: any, @ClinicId() clinicId: string | null) {
    return this.financialService.getSummary(user.sub, clinicId ?? undefined);
  }

  @Get('patient/:patientId')
  getPatientFinancial(
    @Param('patientId') patientId: string,
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
  ) {
    return this.financialService.getPatientFinancial(
      patientId,
      user.sub,
      clinicId ?? undefined,
    );
  }
}




