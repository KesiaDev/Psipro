import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { SyncPaymentsBodyDto } from './dto/sync-payments-body.dto';
import { SyncAppointmentsQueryDto } from './dto/sync-appointments-query.dto';
import { SyncPaymentsService } from './sync-payments.service';

@Controller('sync/payments')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class SyncPaymentsController {
  constructor(private readonly syncPaymentsService: SyncPaymentsService) {}

  @Get()
  getPayments(
    @CurrentUser() _user: any,
    @CurrentClinicId() clinicId: string,
    @Query() query: SyncAppointmentsQueryDto,
  ) {
    return this.syncPaymentsService.getPayments(clinicId, query.updatedAfter);
  }

  @Post()
  syncPayments(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() body: SyncPaymentsBodyDto,
  ) {
    return this.syncPaymentsService.syncPayments(user.sub, clinicId, body.payments);
  }
}
