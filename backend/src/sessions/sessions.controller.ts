import { Controller, Get, Post, Body, Param, UseGuards, Query } from '@nestjs/common';
import { SessionsService } from './sessions.service';
import { CreateSessionDto } from './dto/create-session.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicContextGuard } from '../common/guards/clinic-context.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ClinicId } from '../common/decorators/clinic-id.decorator';

@Controller('sessions')
@UseGuards(JwtAuthGuard, ClinicContextGuard)
export class SessionsController {
  constructor(private readonly sessionsService: SessionsService) {}

  @Post()
  create(
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
    @Body() createSessionDto: CreateSessionDto,
  ) {
    return this.sessionsService.create(user.sub, clinicId ?? undefined, createSessionDto);
  }

  @Get()
  findAll(
    @CurrentUser() user: any,
    @Query('patientId') patientId?: string,
    @ClinicId() clinicId?: string | null,
  ) {
    if (patientId) {
      return this.sessionsService.findByPatient(patientId, user.sub);
    }
    return this.sessionsService.findAll(user.sub, clinicId ?? undefined);
  }
}

