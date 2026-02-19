import { Module, Global } from '@nestjs/common';
import { ClinicContextHelper } from './clinic-context.helper';
import { ClinicGuard } from './guards/clinic.guard';

@Global()
@Module({
  providers: [ClinicContextHelper, ClinicGuard],
  exports: [ClinicContextHelper, ClinicGuard],
})
export class CommonModule {}
