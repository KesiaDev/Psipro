import { Controller, Get, UseGuards } from '@nestjs/common';
import { AppointmentsService } from './appointments.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicContextGuard } from '../common/guards/clinic-context.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ClinicId } from '../common/decorators/clinic-id.decorator';

@Controller('appointments')
@UseGuards(JwtAuthGuard, ClinicContextGuard)
export class AppointmentsController {
  constructor(private readonly appointmentsService: AppointmentsService) {}

  @Get()
  findAll(@CurrentUser() user: any, @ClinicId() clinicId: string | null) {
    return this.appointmentsService.findAll(user.sub, clinicId ?? undefined);
  }
}

