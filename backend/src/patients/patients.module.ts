import { Module } from '@nestjs/common';
import { PatientsController } from './patients.controller';
import { PatientsService } from './patients.service';
import { PatientsImportService } from './patients-import.service';
import { CommonModule } from '../common/common.module';
import { PrismaModule } from '../prisma/prisma.module';

@Module({
  imports: [PrismaModule, CommonModule],
  controllers: [PatientsController],
  providers: [PatientsService, PatientsImportService],
  exports: [PatientsService],
})
export class PatientsModule {}




