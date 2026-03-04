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
  ) {
    if (!file?.buffer) {
      throw new BadRequestException('Arquivo não enviado');
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
        clinicId,
      );
    }

    return this.patientsImportService.importFromExcel(
      file.buffer,
      clinicId,
      user.sub,
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
