import {
  BadRequestException,
  Controller,
  Get,
  Post,
  Body,
  Patch,
  Param,
  Query,
  UseGuards,
  UseInterceptors,
  UploadedFile,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { PatientsService } from './patients.service';
import { CreatePatientDto } from './dto/create-patient.dto';
import { UpdatePatientDto } from './dto/update-patient.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicContextGuard } from '../common/guards/clinic-context.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ClinicId } from '../common/decorators/clinic-id.decorator';

@Controller('patients')
@UseGuards(JwtAuthGuard, ClinicContextGuard)
export class PatientsController {
  constructor(private readonly patientsService: PatientsService) {}

  @Post()
  create(
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
    @Body() createPatientDto: CreatePatientDto,
  ) {
    return this.patientsService.create(
      user.sub,
      clinicId ?? undefined,
      createPatientDto,
    );
  }

  /**
   * POST /patients/import
   *
   * Importação de pacientes via Excel (multipart/form-data).
   * Campos esperados:
   * - file: arquivo .xlsx/.xls
   * - mapping: JSON string com o mapeamento de colunas (mesmo do Web)
   * - clinicId (opcional): clínica ativa para associar os pacientes
   */
  @Post('import')
  @UseInterceptors(FileInterceptor('file'))
  async importPatients(
    @CurrentUser() user: any,
    @ClinicId() clinicId: string | null,
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
      clinicId ?? undefined,
    );
  }

  @Get()
  findAll(@CurrentUser() user: any, @ClinicId() clinicId: string | null) {
    return this.patientsService.findAll(user.sub, clinicId ?? undefined);
  }

  @Get(':id')
  findOne(@Param('id') id: string, @CurrentUser() user: any) {
    return this.patientsService.findOne(id, user.sub);
  }

  @Patch(':id')
  update(
    @Param('id') id: string,
    @CurrentUser() user: any,
    @Body() updatePatientDto: UpdatePatientDto,
  ) {
    return this.patientsService.update(id, user.sub, updatePatientDto);
  }
}

