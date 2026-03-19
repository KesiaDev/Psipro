import { Controller, Get, Post, Patch, Delete, Body, Param, UseGuards } from '@nestjs/common';
import { FinancialService, CreateFinancialRecordDto, UpdateFinancialRecordDto, CreateChargeDto, UpdateChargeDto, PayChargeDto } from './financial.service';
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

  @Get('records')
  findAllRecords(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.financialService.findAllRecords(user.sub, clinicId);
  }

  @Post('records')
  createRecord(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: CreateFinancialRecordDto,
  ) {
    return this.financialService.createRecord(user.sub, clinicId, dto);
  }

  @Patch('records/:id')
  updateRecord(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: UpdateFinancialRecordDto,
  ) {
    return this.financialService.updateRecord(id, user.sub, clinicId, dto);
  }

  @Delete('records/:id')
  deleteRecord(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.financialService.deleteRecord(id, user.sub, clinicId);
  }

  @Get('patient/:patientId')
  getPatientFinancial(
    @Param('patientId') patientId: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.financialService.getPatientFinancial(patientId, user.sub, clinicId);
  }

  // ─── Charges (cobranças vinculadas a sessões/pacientes) ───────────────────

  @Get('charges')
  findAllCharges(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.financialService.findAllCharges(user.sub, clinicId);
  }

  @Get('charges/:id')
  findOneCharge(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.financialService.findOneCharge(id, user.sub, clinicId);
  }

  @Post('charges')
  createCharge(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: CreateChargeDto,
  ) {
    return this.financialService.createCharge(user.sub, clinicId, dto);
  }

  @Patch('charges/:id')
  updateCharge(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: UpdateChargeDto,
  ) {
    return this.financialService.updateCharge(id, user.sub, clinicId, dto);
  }

  @Patch('charges/:id/pay')
  payCharge(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: PayChargeDto,
  ) {
    return this.financialService.payCharge(id, user.sub, clinicId, dto);
  }

  @Delete('charges/:id')
  deleteCharge(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.financialService.deleteCharge(id, user.sub, clinicId);
  }
}




