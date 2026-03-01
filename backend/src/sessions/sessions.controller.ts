import { Controller, Get, Post, Body, UseGuards, Query } from '@nestjs/common';
import { SessionsService } from './sessions.service';
import { CreateSessionDto } from './dto/create-session.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';

@Controller('sessions')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class SessionsController {
  constructor(private readonly sessionsService: SessionsService) {}

  @Post()
  create(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() createSessionDto: CreateSessionDto,
  ) {
    return this.sessionsService.create(user.sub, createSessionDto, clinicId);
  }

  @Get('stats')
  getStats(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.sessionsService.getStats(user.sub, clinicId);
  }

  @Get()
  findAll(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Query('patientId') patientId?: string,
  ) {
    if (patientId) {
      return this.sessionsService.findByPatient(patientId, user.sub, clinicId);
    }
    return this.sessionsService.findAll(user.sub, clinicId);
  }
}

