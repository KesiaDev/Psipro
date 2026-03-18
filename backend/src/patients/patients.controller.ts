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
import { PatientsImportService } from './patients-import.service';
import { PatientPatternsService } from './patient-patterns.service';
import { EmotionalEvolutionService } from './emotional-evolution.service';
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
  constructor(
    private readonly patientsService: PatientsService,
    private readonly patientsImportService: PatientsImportService,
    private readonly patientPatternsService: PatientPatternsService,
    private readonly emotionalEvolutionService: EmotionalEvolutionService,
  ) {}

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

  @Get(':id/patterns')
  getPatterns(
    @Param('id') id: string,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.patientPatternsService.getPatterns(id, clinicId);
  }

  @Get(':id/emotional-evolution')
  getEmotionalEvolution(
    @Param('id') id: string,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.emotionalEvolutionService.getEmotionalEvolution(id, clinicId);
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
   * Sem mapping: usa formato fixo (Nome completo, Data de nascimento, E-mail, Telefone, Gênero).
   * Com mapping: usa mapeamento customizado (legado Web).
   */
  @Post('import')
  @UseInterceptors(FileInterceptor('file'))
  async importPatients(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @UploadedFile() file: { buffer: Buffer; originalname?: string } | undefined,
    @Body('mapping') mapping?: string,
    @Body('clinicId') formClinicId?: string,
  ) {
    if (!file?.buffer) {
      throw new BadRequestException('Arquivo não enviado');
    }

    // Garantir clinicId: header é validado pelo ClinicGuard; form é fallback se vier
    const effectiveClinicId = clinicId?.trim() || formClinicId?.trim();
    if (!effectiveClinicId) {
      throw new BadRequestException(
        'Clínica não identificada. Envie o header X-Clinic-Id ou clinicId no formulário.',
      );
    }

    const ext = (file.originalname || '').toLowerCase();
    if (!ext.endsWith('.xlsx') && !ext.endsWith('.xls')) {
      throw new BadRequestException('Arquivo deve ser .xlsx ou .xls');
    }

    if (mapping) {
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
        effectiveClinicId,
      );
    }

    return this.patientsImportService.importFromExcel(
      file.buffer,
      effectiveClinicId,
      user.sub,
    );
  }

  @Patch(':id/anonymize')
  anonymize(
    @Param('id') id: string,
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
  ) {
    return this.patientsService.anonymize(id, clinicId, user.sub);
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
