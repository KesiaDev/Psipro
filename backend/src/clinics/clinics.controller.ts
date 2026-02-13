import {
  Controller,
  Get,
  Post,
  Put,
  Delete,
  Body,
  Param,
  UseGuards,
  Request,
} from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicsService } from './clinics.service';
import { CreateClinicDto } from './dto/create-clinic.dto';
import { UpdateClinicDto } from './dto/update-clinic.dto';
import { InviteUserDto } from './dto/invite-user.dto';
import { UpdateClinicUserDto } from './dto/update-clinic-user.dto';

@Controller('clinics')
@UseGuards(JwtAuthGuard)
export class ClinicsController {
  constructor(private readonly clinicsService: ClinicsService) {}

  @Post()
  create(@Request() req, @Body() createClinicDto: CreateClinicDto) {
    return this.clinicsService.create(req.user.id, createClinicDto);
  }

  @Get()
  findAll(@Request() req) {
    return this.clinicsService.findAll(req.user.id);
  }

  @Get(':id')
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

  @Post(':id/invite')
  inviteUser(
    @Request() req,
    @Param('id') id: string,
    @Body() inviteUserDto: InviteUserDto,
  ) {
    return this.clinicsService.inviteUser(id, req.user.id, inviteUserDto);
  }

  @Put(':id/users/:userId')
  updateUser(
    @Request() req,
    @Param('id') clinicId: string,
    @Param('userId') targetUserId: string,
    @Body() updateDto: UpdateClinicUserDto,
  ) {
    return this.clinicsService.updateUser(clinicId, targetUserId, req.user.id, updateDto);
  }

  @Delete(':id/users/:userId')
  removeUser(
    @Request() req,
    @Param('id') clinicId: string,
    @Param('userId') targetUserId: string,
  ) {
    return this.clinicsService.removeUser(clinicId, targetUserId, req.user.id);
  }

  @Get(':id/stats')
  getStats(@Request() req, @Param('id') id: string) {
    return this.clinicsService.getClinicStats(id, req.user.id);
  }
}




