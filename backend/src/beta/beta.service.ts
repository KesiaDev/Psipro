import { BadRequestException, Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBetaRequestDto } from './dto/create-beta-request.dto';

@Injectable()
export class BetaService {
  constructor(private readonly prisma: PrismaService) {}

  async create(dto: CreateBetaRequestDto) {
    const name = (dto.name || dto.fullName || '').trim();
    if (!name || name.length < 2) {
      throw new BadRequestException('Nome é obrigatório (envie name ou fullName)');
    }

    const extra: string[] = [];
    if (dto.city) extra.push(`Cidade: ${dto.city}`);
    if (dto.state) extra.push(`Estado: ${dto.state}`);
    if (dto.practiceType) extra.push(`Tipo de prática: ${dto.practiceType}`);
    if (dto.expectations) extra.push(`Expectativas: ${dto.expectations}`);
    const message = dto.message
      ? extra.length > 0
        ? `${dto.message}\n\n${extra.join('\n')}`
        : dto.message
      : extra.length > 0
        ? extra.join('\n')
        : null;

    return this.prisma.betaRequest.create({
      data: {
        name,
        email: dto.email.trim(),
        clinicName: dto.clinicName?.trim() || null,
        message: message || null,
      },
    });
  }
}
