import { Controller, Get, Post, Body, Param, UseGuards } from '@nestjs/common';
import { PaymentsService } from './payments.service';
import { CreatePaymentDto } from './dto/create-payment.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';

@Controller('payments')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  @Post()
  create(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() createPaymentDto: CreatePaymentDto,
  ) {
    return this.paymentsService.create(user.sub, createPaymentDto, clinicId);
  }

  @Get('patient/:patientId')
  findByPatient(
    @Param('patientId') patientId: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.paymentsService.findByPatient(patientId, user.sub, clinicId);
  }
}




