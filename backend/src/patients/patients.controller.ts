import {
  BadRequestException,
  Controller,
  Delete,
  Get,
  Post,
  Body,
  Patch,
  Param,
  UseGuards,
  UseInterceptors,
  UploadedFile,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { PatientsService } from './patients.service';
import { CreatePatientDto } from './dto/create-patient.dto';
import { UpdatePatientDto } from './dto/update-patient.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('patients')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist', 'assistant')
export class PatientsController {
  constructor(private readonly patientsService: PatientsService) {}

  @Get()
  findAll(@CurrentClinicId() clinicId: string) {
    return this.patientsService.findAll(clinicId);
  }

  @Get('count')
  getCount(@CurrentClinicId() clinicId: string) {
    return this.patientsService.getCount(clinicId);
  }

  @Get('recent')
  getRecent(@CurrentClinicId() clinicId: string) {
    return this.patientsService.getRecent(clinicId);
  }

  @Get(':id')
  findOne(@Param('id') id: string, @CurrentClinicId() clinicId: string) {
    return this.patientsService.findOne(id, clinicId);
  }

  @Post()
  create(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() createPatientDto: CreatePatientDto,
  ) {
    return this.patientsService.create(createPatientDto, clinicId, user.sub);
  }

  /**
   * POST /patients/import
   *
   * Importação de pacientes via Excel (multipart/form-data).
   * Campos esperados:
   * - file: arquivo .xlsx/.xls
   * - mapping: JSON string com o mapeamento de colunas (mesmo do Web)
   *
   * Pacientes são associados à clínica ativa (X-Clinic-Id).
   */
  @Post('import')
  @UseInterceptors(FileInterceptor('file'))
  async importPatients(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @UploadedFile() file: any,
    @Body('mapping') mapping?: string,
  ) {
    if (!file) {
      throw new BadRequestException('Arquivo não enviado');
    }
    if (!mapping) {
      throw new BadRequestException('Mapping não enviado');
    }

    let parsedMapping: Record<string, string>;
    try {
      parsedMapping = JSON.parse(mapping);
    } catch {
      throw new BadRequestException('Mapping inválido');
    }

    return this.patientsService.importFromExcel(
      user.sub,
      file.buffer,
      parsedMapping,
      clinicId,
    );
  }

  @Patch(':id')
  update(
    @Param('id') id: string,
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() updatePatientDto: UpdatePatientDto,
  ) {
    return this.patientsService.update(id, clinicId, updatePatientDto, user.sub);
  }

  @Delete(':id')
  delete(@Param('id') id: string, @CurrentUser() user: { sub: string }, @CurrentClinicId() clinicId: string) {
    return this.patientsService.delete(id, clinicId, user.sub);
  }
}
