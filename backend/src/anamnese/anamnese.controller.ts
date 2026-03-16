import { Body, Controller, Delete, Get, Param, Post, Put, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { AnamneseService } from './anamnese.service';

@Controller('anamnese')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist', 'assistant')
export class AnamneseController {
  constructor(private readonly anamneseService: AnamneseService) {}

  @Get('models')
  getModels(@CurrentClinicId() clinicId: string) {
    return this.anamneseService.getModels(clinicId);
  }

  @Post('models')
  createModel(
    @CurrentClinicId() clinicId: string,
    @Body() body: { nome: string; isDefault?: boolean },
  ) {
    return this.anamneseService.createModel(clinicId, body);
  }

  @Put('models/:id')
  updateModel(
    @CurrentClinicId() clinicId: string,
    @Param('id') id: string,
    @Body() body: { nome?: string; isDefault?: boolean },
  ) {
    return this.anamneseService.updateModel(clinicId, id, body);
  }

  @Delete('models/:id')
  deleteModel(@CurrentClinicId() clinicId: string, @Param('id') id: string) {
    return this.anamneseService.deleteModel(clinicId, id);
  }

  @Get('models/:modeloId/campos')
  getCampos(
    @Param('modeloId') modeloId: string,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.anamneseService.getCampos(modeloId, clinicId);
  }

  @Post('models/:modeloId/campos')
  syncCampos(
    @Param('modeloId') modeloId: string,
    @CurrentClinicId() clinicId: string,
    @Body() body: { campos: Array<{ id?: string; tipo: string; label: string; opcoes?: string; obrigatorio?: boolean; ordem?: number }> },
  ) {
    return this.anamneseService.syncCampos(modeloId, clinicId, body.campos);
  }

  @Get('patients/:patientId/preenchidas')
  getPreenchidas(
    @Param('patientId') patientId: string,
    @CurrentClinicId() clinicId: string,
  ) {
    return this.anamneseService.getPreenchidas(patientId, clinicId);
  }

  @Post('preenchidas')
  createPreenchida(
    @CurrentClinicId() clinicId: string,
    @Body() body: {
      patientId: string;
      modeloId: string;
      respostas: Record<string, unknown>;
      assinaturaPath?: string;
    },
  ) {
    return this.anamneseService.createPreenchida(clinicId, body);
  }
}
