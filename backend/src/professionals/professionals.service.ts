import { Injectable } from '@nestjs/common';
import { ClinicsService } from '../clinics/clinics.service';
import { CreateProfessionalDto } from './dto/create-professional.dto';
import { UpdateProfessionalDto } from './dto/update-professional.dto';

@Injectable()
export class ProfessionalsService {
  constructor(private readonly clinicsService: ClinicsService) {}

  /**
   * Convida usuário existente para a clínica como profissional.
   * POST /api/professionals
   */
  async create(clinicId: string, currentUserId: string, dto: CreateProfessionalDto) {
    const targetClinicId = dto.clinicId || clinicId;
    return this.clinicsService.inviteUser(targetClinicId, currentUserId, {
      email: dto.email,
      role: dto.role || 'psychologist',
    });
  }

  /**
   * Atualiza profissional na clínica (ClinicUser).
   * PUT /api/professionals/:userId
   */
  async update(
    clinicId: string,
    currentUserId: string,
    targetUserId: string,
    dto: UpdateProfessionalDto,
  ) {
    return this.clinicsService.updateUser(clinicId, targetUserId, currentUserId, dto);
  }

  /**
   * Remove profissional da clínica.
   * DELETE /api/professionals/:userId
   */
  async remove(clinicId: string, currentUserId: string, targetUserId: string) {
    return this.clinicsService.removeUser(clinicId, targetUserId, currentUserId);
  }
}
