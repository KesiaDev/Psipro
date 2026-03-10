import {
  Controller,
  Get,
  Post,
  Put,
  Patch,
  Delete,
  Body,
  Param,
  UseGuards,
  Request,
} from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { ClinicsService } from './clinics.service';
import { CreateClinicDto } from './dto/create-clinic.dto';
import { UpdateClinicDto } from './dto/update-clinic.dto';
import { InviteUserDto } from './dto/invite-user.dto';
import { UpdateClinicUserDto } from './dto/update-clinic-user.dto';

@Controller('clinics')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles('admin')
export class ClinicsController {
  constructor(private readonly clinicsService: ClinicsService) {}

  @Post()
  @Roles('admin')
  create(@Request() req, @Body() createClinicDto: CreateClinicDto) {
    return this.clinicsService.create(req.user.id, createClinicDto);
  }

  @Get()
  @Roles('admin', 'psychologist')
  findAll(@Request() req) {
    return this.clinicsService.findAll(req.user.id);
  }

  @Get(':id')
  @Roles('admin', 'psychologist')
  findOne(@Request() req, @Param('id') id: string) {
    return this.clinicsService.findOne(id, req.user.id);
  }

  @Put(':id')
  update(
    @Request() req,
    @Param('id') id: string,
    @Body() updateClinicDto: UpdateClinicDto,
  ) {
    return this.clinicsService.update(id, req.user.id, updateClinicDto);
  }

  @Patch(':id/status')
  @Roles('admin')
  updateStatus(
    @Request() req,
    @Param('id') id: string,
    @Body() body: { status: string },
  ) {
    return this.clinicsService.update(id, req.user.id, { status: body?.status });
  }

  @Delete(':id')
  @Roles('admin')
  delete(@Request() req, @Param('id') id: string) {
    return this.clinicsService.delete(id, req.user.id);
  }

  @Post(':id/invite')
  inviteUser(
    @Request() req,
    @Param('id') id: string,
    @Body() inviteUserDto: InviteUserDto,
  ) {
    return this.clinicsService.inviteUser(id, req.user.id, inviteUserDto);
  }

  @Put(':id/users/:userId')
  @Roles('admin')
  updateUser(
    @Request() req,
    @Param('id') clinicId: string,
    @Param('userId') targetUserId: string,
    @Body() updateDto: UpdateClinicUserDto,
  ) {
    return this.clinicsService.updateUser(clinicId, targetUserId, req.user.id, updateDto);
  }

  @Delete(':id/users/:userId')
  @Roles('admin')
  removeUser(
    @Request() req,
    @Param('id') clinicId: string,
    @Param('userId') targetUserId: string,
  ) {
    return this.clinicsService.removeUser(clinicId, targetUserId, req.user.id);
  }

  @Get(':clinicId/professionals')
  @Roles('admin')
  getProfessionals(@Request() req, @Param('clinicId') clinicId: string) {
    return this.clinicsService.getProfessionals(clinicId, req.user.id);
  }

  @Get(':id/stats')
  @Roles('admin')
  getStats(@Request() req, @Param('id') id: string) {
    return this.clinicsService.getClinicStats(id, req.user.id);
  }
}




