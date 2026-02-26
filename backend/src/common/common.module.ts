import { Global, Module } from '@nestjs/common';
import { PatientAccessHelper } from './helpers/patient-access.helper';

@Global()
@Module({
  providers: [PatientAccessHelper],
  exports: [PatientAccessHelper],
})
export class CommonModule {}
