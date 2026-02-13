import { Controller, Get, Post, Body, Param, UseGuards } from '@nestjs/common';
import { PaymentsService } from './payments.service';
import { CreatePaymentDto } from './dto/create-payment.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('payments')
@UseGuards(JwtAuthGuard)
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  @Post()
  create(@CurrentUser() user: any, @Body() createPaymentDto: CreatePaymentDto) {
    return this.paymentsService.create(user.sub, createPaymentDto);
  }

  @Get('patient/:patientId')
  findByPatient(@Param('patientId') patientId: string, @CurrentUser() user: any) {
    return this.paymentsService.findByPatient(patientId, user.sub);
  }
}




