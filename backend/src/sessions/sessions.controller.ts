import { Controller, Get, Post, Body, Param, UseGuards, Query } from '@nestjs/common';
import { SessionsService } from './sessions.service';
import { CreateSessionDto } from './dto/create-session.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('sessions')
@UseGuards(JwtAuthGuard)
export class SessionsController {
  constructor(private readonly sessionsService: SessionsService) {}

  @Post()
  create(@CurrentUser() user: any, @Body() createSessionDto: CreateSessionDto) {
    return this.sessionsService.create(user.sub, createSessionDto);
  }

  @Get()
  findAll(
    @CurrentUser() user: any,
    @Query('patientId') patientId?: string,
    @Query('clinicId') clinicId?: string,
  ) {
    if (patientId) {
      return this.sessionsService.findByPatient(patientId, user.sub);
    }
    return this.sessionsService.findAll(user.sub, clinicId);
  }
}

