import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import { AppointmentsService } from './appointments.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('appointments')
@UseGuards(JwtAuthGuard)
export class AppointmentsController {
  constructor(private readonly appointmentsService: AppointmentsService) {}

  @Get()
  findAll(@CurrentUser() user: any, @Query('clinicId') clinicId?: string) {
    return this.appointmentsService.findAll(user.sub, clinicId);
  }
}

