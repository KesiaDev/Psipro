import {
  Controller,
  Post,
  Get,
  Patch,
  Param,
  Body,
  Query,
  UseGuards,
  Request,
} from '@nestjs/common';
import { LgpdService } from './lgpd.service';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@UseGuards(JwtAuthGuard, ClinicGuard)
@Controller('lgpd')
export class LgpdController {
  constructor(private readonly lgpdService: LgpdService) {}

  /** POST /api/lgpd/consent — registra consentimento LGPD */
  @Post('consent')
  recordConsent(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() body: { patientId?: string; type: string; version?: string },
    @Request() req: any,
  ) {
    return this.lgpdService.recordConsent({
      userId: user.sub,
      clinicId,
      patientId: body.patientId,
      type: body.type,
      version: body.version,
      ipAddress: req.ip,
      userAgent: req.headers['user-agent'],
    });
  }

  /** GET /api/lgpd/consent — lista consentimentos da clínica/paciente */
  @Get('consent')
  getConsents(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Query('patientId') patientId?: string,
  ) {
    return this.lgpdService.getConsents(user.sub, clinicId, patientId);
  }

  /** PATCH /api/lgpd/consent/:id/revoke — revoga consentimento */
  @Patch('consent/:id/revoke')
  revokeConsent(
    @Param('id') consentId: string,
    @CurrentClinicId() clinicId: string,
    @CurrentUser() user: { sub: string },
  ) {
    return this.lgpdService.revokeConsent(consentId, clinicId, user.sub);
  }

  /** POST /api/lgpd/anonymize/:patientId — anonimiza dados do paciente */
  @Post('anonymize/:patientId')
  anonymizePatient(
    @Param('patientId') patientId: string,
    @CurrentClinicId() clinicId: string,
    @CurrentUser() user: { sub: string },
  ) {
    return this.lgpdService.anonymizePatient(patientId, clinicId, user.sub);
  }
}
