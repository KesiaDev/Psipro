import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import { DocumentsService } from './documents.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicContextGuard } from '../common/guards/clinic-context.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { ClinicId } from '../common/decorators/clinic-id.decorator';

@Controller('documents')
@UseGuards(JwtAuthGuard, ClinicContextGuard)
export class DocumentsController {
  constructor(private readonly documentsService: DocumentsService) {}

  @Get()
  findAll(
    @CurrentUser() user: any,
    @Query('patientId') patientId?: string,
    @ClinicId() clinicId?: string | null,
  ) {
    return this.documentsService.findAll(
      user.sub,
      patientId,
      clinicId ?? undefined,
    );
  }
}




