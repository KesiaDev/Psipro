import { Controller, Post, Body, Query, UseGuards, Req } from '@nestjs/common';
import { Request } from 'express';
import { Throttle } from '@nestjs/throttler';
import { IntakeService } from './intake.service';
import { CreateIntakeDto } from './dto/create-intake.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';

@Controller('patients')
export class IntakeController {
  constructor(private readonly intakeService: IntakeService) {}

  /**
   * POST /api/patients/intake-token
   * Rota autenticada — terapeuta gera link de intake para compartilhar com paciente.
   */
  @Post('intake-token')
  @UseGuards(JwtAuthGuard, ClinicGuard)
  generateToken(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
  ) {
    return this.intakeService.generateToken(user.sub, clinicId);
  }

  /**
   * POST /api/patients/intake?token=XYZ
   * Rota PÚBLICA — paciente preenche o formulário sem autenticação.
   * Rate limited: 5 requisições por minuto por IP.
   */
  @Post('intake')
  @Throttle({ default: { limit: 5, ttl: 60_000 } })
  processIntake(
    @Query('token') token: string,
    @Body() dto: CreateIntakeDto,
    @Req() req: Request,
  ) {
    return this.intakeService.processIntake(token, dto, {
      ip: req.socket?.remoteAddress,
      headers: req.headers as Record<string, string | string[] | undefined>,
    });
  }
}
