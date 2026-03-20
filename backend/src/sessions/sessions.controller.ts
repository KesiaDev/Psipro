import { Controller, Get, Post, Body, Patch, Delete, Param, UseGuards, Query } from '@nestjs/common';
import { SessionsService } from './sessions.service';
import { CreateSessionDto } from './dto/create-session.dto';
import { UpdateSessionDto } from './dto/update-session.dto';
import { VoiceNoteDto } from './dto/voice-note.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';

@Controller('sessions')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist', 'assistant')
export class SessionsController {
  constructor(private readonly sessionsService: SessionsService) {}

  @Post('voice-note')
  createVoiceNote(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: VoiceNoteDto,
  ) {
    return this.sessionsService.updateVoiceNote(
      dto.sessionId,
      dto.transcript,
      user.sub,
      clinicId,
    );
  }

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

  @Get(':id')
  async findOne(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
  ) {
    const session = await this.sessionsService.findOne(id, user.sub, clinicId);
    return this.sessionsService.formatForDashboard(session);
  }

  @Patch(':id')
  update(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() updateSessionDto: UpdateSessionDto,
  ) {
    return this.sessionsService.update(id, user.sub, clinicId, updateSessionDto);
  }

  @Delete(':id')
  async delete(
    @Param('id') id: string,
    @CurrentClinicId() clinicId: string,
  ) {
    await this.sessionsService.delete(id, clinicId);
    return { success: true };
  }
}

