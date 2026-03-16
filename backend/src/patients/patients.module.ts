import { Module } from '@nestjs/common';
import { PatientsController } from './patients.controller';
import { PatientsService } from './patients.service';
import { PatientsImportService } from './patients-import.service';
import { PatientPatternsService } from './patient-patterns.service';
import { EmotionalEvolutionService } from './emotional-evolution.service';
import { CommonModule } from '../common/common.module';
import { PrismaModule } from '../prisma/prisma.module';
import { VoiceModule } from '../voice/voice.module';

@Module({
  imports: [PrismaModule, CommonModule, VoiceModule],
  controllers: [PatientsController],
  providers: [
    PatientsService,
    PatientsImportService,
    PatientPatternsService,
    EmotionalEvolutionService,
  ],
  exports: [PatientsService],
})
export class PatientsModule {}




