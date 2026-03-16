import {
  Controller,
  Post,
  Put,
  Delete,
  Body,
  Param,
  UseGuards,
} from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { ClinicGuard } from '../common/guards/clinic.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';
import { ProfessionalsService } from './professionals.service';
import { CreateProfessionalDto } from './dto/create-professional.dto';
import { UpdateProfessionalDto } from './dto/update-professional.dto';

@Controller('professionals')
@UseGuards(JwtAuthGuard, ClinicGuard, RolesGuard)
@Roles('admin', 'psychologist')
export class ProfessionalsController {
  constructor(private readonly professionalsService: ProfessionalsService) {}

  @Post()
  create(
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() dto: CreateProfessionalDto,
  ) {
    return this.professionalsService.create(clinicId, user.sub, dto);
  }

  @Put(':id')
  update(
    @Param('id') userId: string,
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
    @Body() dto: UpdateProfessionalDto,
  ) {
    return this.professionalsService.update(clinicId, user.sub, userId, dto);
  }

  @Delete(':id')
  remove(
    @Param('id') userId: string,
    @CurrentUser() user: { sub: string },
    @CurrentClinicId() clinicId: string,
  ) {
    return this.professionalsService.remove(clinicId, user.sub, userId);
  }
}
