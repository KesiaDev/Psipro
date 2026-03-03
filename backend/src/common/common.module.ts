import { Module, Global } from '@nestjs/common';
import { ClinicContextHelper } from './clinic-context.helper';
import { ClinicGuard } from './guards/clinic.guard';
import { RolesGuard } from './guards/roles.guard';

@Global()
@Module({
  providers: [ClinicContextHelper, ClinicGuard, RolesGuard],
  exports: [ClinicContextHelper, ClinicGuard, RolesGuard],
})
export class CommonModule {}
