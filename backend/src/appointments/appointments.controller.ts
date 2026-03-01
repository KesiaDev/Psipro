import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  UseGuards,
} from '@nestjs/common';
import { AppointmentsService } from './appointments.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { CreateAppointmentDto } from './dto/create-appointment.dto';
import { UpdateAppointmentDto } from './dto/update-appointment.dto';

@Controller('appointments')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class AppointmentsController {
  constructor(private readonly appointmentsService: AppointmentsService) {}

  @Get()
  findAll(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.appointmentsService.findAll(user.sub, clinicId);
  }

  @Get('today')
  getToday(@CurrentUser() user: any, @CurrentClinicId() clinicId: string) {
    return this.appointmentsService.getToday(user.sub, clinicId);
  }

  @Get(':id')
  async findOne(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.appointmentsService.findOne(id, user.sub, clinicId);
  }

  @Post()
  create(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: CreateAppointmentDto,
  ) {
    return this.appointmentsService.create(user.sub, dto, clinicId);
  }

  @Put(':id')
  update(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: UpdateAppointmentDto,
  ) {
    return this.appointmentsService.update(id, user.sub, dto, clinicId);
  }

  @Delete(':id')
  delete(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.appointmentsService.delete(id, user.sub, clinicId);
  }
}

