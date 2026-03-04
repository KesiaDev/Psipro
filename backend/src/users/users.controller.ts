import { Body, Controller, Get, Put, UseGuards } from '@nestjs/common';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { AuthService } from '../auth/auth.service';
import { UpdateProfileDto } from './dto/update-profile.dto';
import { ChangePasswordDto } from './dto/change-password.dto';

@Controller('users')
@UseGuards(JwtAuthGuard)
export class UsersController {
  constructor(private readonly authService: AuthService) {}

  /**
   * GET /api/users/me
   * Retorna perfil do usuário autenticado (alias para /auth/me).
   */
  @Get('me')
  getMe(@CurrentUser() user: { sub: string }) {
    return this.authService.validateToken(user.sub);
  }

  /**
   * PUT /api/users/me
   * Atualiza perfil do usuário (nome, email, phone, license).
   */
  @Put('me')
  updateMe(
    @CurrentUser() user: { sub: string },
    @Body() dto: UpdateProfileDto,
  ) {
    return this.authService.updateProfile(user.sub, dto);
  }

  /**
   * PUT /api/users/password
   * Altera senha do usuário.
   */
  @Put('password')
  changePassword(
    @CurrentUser() user: { sub: string },
    @Body() dto: ChangePasswordDto,
  ) {
    return this.authService.changePassword(
      user.sub,
      dto.currentPassword,
      dto.newPassword,
    );
  }
}
