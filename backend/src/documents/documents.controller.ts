import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import { DocumentsService } from './documents.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';

@Controller('documents')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist', 'assistant')
export class DocumentsController {
  constructor(private readonly documentsService: DocumentsService) {}

  @Get()
  findAll(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Query('patientId') patientId?: string,
  ) {
    return this.documentsService.findAll(user.sub, clinicId, patientId);
  }
}




