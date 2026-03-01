import { Controller, Get, Param, UseGuards } from '@nestjs/common';
import { FinancialService } from './financial.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('financial')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class FinancialController {
  constructor(private readonly financialService: FinancialService) {}

  @Get('summary')
  getSummary(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.financialService.getSummary(user.sub, clinicId);
  }

  @Get('patient/:patientId')
  getPatientFinancial(
    @Param('patientId') patientId: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.financialService.getPatientFinancial(patientId, user.sub, clinicId);
  }
}




