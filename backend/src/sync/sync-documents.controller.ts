import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { SyncDocumentsBodyDto } from './dto/sync-documents-body.dto';
import { SyncDocumentsQueryDto } from './dto/sync-documents-query.dto';
import { SyncDocumentsService } from './sync-documents.service';

@Controller('sync/documents')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class SyncDocumentsController {
  constructor(private readonly syncDocumentsService: SyncDocumentsService) {}

  /**
   * GET /api/sync/documents
   * Retorna documentos da clínica para sincronização.
   */
  @Get()
  getDocuments(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Query() query: SyncDocumentsQueryDto,
  ) {
    return this.syncDocumentsService.getDocuments(
      user.sub,
      clinicId,
      query.patientId,
      query.updatedAfter,
    );
  }

  /**
   * POST /api/sync/documents
   * Recebe lista do app e resolve conflitos no backend.
   */
  @Post()
  syncDocuments(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() body: SyncDocumentsBodyDto,
  ) {
    return this.syncDocumentsService.syncDocuments(user.sub, clinicId, body.documents);
  }
}
