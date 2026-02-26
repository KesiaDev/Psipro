import { Controller, Get, Post, Body, Param, UseGuards } from '@nestjs/common';
import { PaymentsService } from './payments.service';
import { CreatePaymentDto } from './dto/create-payment.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicContextGuard } from '../common/guards/clinic-context.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ClinicId } from '../common/decorators/clinic-id.decorator';

@Controller('payments')
@UseGuards(JwtAuthGuard, ClinicContextGuard)
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  @Post()
  create(
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
    @Body() createPaymentDto: CreatePaymentDto,
  ) {
    return this.paymentsService.create(user.sub, clinicId ?? undefined, createPaymentDto);
  }

  @Get('patient/:patientId')
  findByPatient(
    @Param('patientId') patientId: string,
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
  ) {
    return this.paymentsService.findByPatient(patientId, user.sub, clinicId ?? undefined);
  }
}




