import {
  Controller,
  Post,
  Body,
  UseGuards,
  Get,
  Headers,
  UnauthorizedException,
  Req,
} from '@nestjs/common';
import { Throttle } from '@nestjs/throttler';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { RefreshDto } from './dto/refresh.dto';
import { HandoffDto } from './dto/handoff.dto';
import { RegisterDto } from './dto/register.dto';
import { SwitchClinicDto } from './dto/switch-clinic.dto';
import { ForgotPasswordDto } from './dto/forgot-password.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @Throttle({ default: { limit: 5, ttl: 60_000 } })
  @Post('login')
  async login(@Body() loginDto: LoginDto, @Req() req: { headers?: { 'user-agent'?: string; 'x-forwarded-for'?: string }; ip?: string }) {
    const deviceInfo = req?.headers?.['user-agent'];
    const ipAddress = req?.ip;
    return this.authService.login(loginDto, { deviceInfo, ipAddress, request: { ip: ipAddress, headers: req?.headers } });
  }

  @Post('logout')
  async logout(@Body() body: RefreshDto, @Req() req?: { ip?: string; headers?: { 'user-agent'?: string } }) {
    await this.authService.logout(body.refreshToken, req ? { ip: req.ip, headers: req.headers } : undefined);
  }

  @Throttle({ default: { limit: 10, ttl: 60_000 } })
  @Post('refresh')
  async refresh(@Body() body: RefreshDto) {
    try {
      return await this.authService.refresh(body.refreshToken);
    } catch (err) {
      if (err instanceof UnauthorizedException) {
        throw err;
      }
      throw new UnauthorizedException('Token inválido');
    }
  }

  @Throttle({ default: { limit: 3, ttl: 60_000 } })
  @Post('register')
  async register(@Body() registerDto: RegisterDto) {
    return this.authService.register(registerDto);
  }

  /**
   * POST /auth/forgot-password
   * Recuperação de senha. Por segurança, sempre retorna 200 (não revela se o e-mail existe).
   * Quando o serviço de e-mail estiver configurado, enviará o link de redefinição.
   */
  @Throttle({ default: { limit: 3, ttl: 60_000 } })
  @Post('forgot-password')
  async forgotPassword(@Body() dto: ForgotPasswordDto) {
    await this.authService.forgotPassword(dto.email);
    return { message: 'Se o e-mail existir, você receberá instruções para redefinir a senha.' };
  }

  /**
   * POST /auth/handoff
   *
   * SSO Android -> Web (token 30s, single-use):
   * - App envia JWT → retorna { handoffToken, redirectUrl }
   * - Web envia handoffToken → retorna { accessToken, refreshToken, user }
   */
  @Post('handoff')
  async handoff(@Body() body: HandoffDto, @Headers('authorization') authorization?: string) {
    const headerToken = authorization?.startsWith('Bearer ')
      ? authorization.slice('Bearer '.length).trim()
      : undefined;

    const token = body?.token || headerToken;
    if (!token) {
      throw new UnauthorizedException('Token inválido');
    }

    return this.authService.handoff(token, body?.returnUrl);
  }

  /**
   * POST /auth/switch-clinic
   *
   * Troca a clínica ativa do usuário. Valida pertencimento via ClinicUser.
   * Retorna novo accessToken com clinicId atualizado. Não altera refreshToken.
   */
  @UseGuards(JwtAuthGuard)
  @Post('switch-clinic')
  async switchClinic(@CurrentUser() user: any, @Body() dto: SwitchClinicDto, @Req() req?: { ip?: string; headers?: { 'user-agent'?: string } }) {
    return this.authService.switchClinic(user.sub, dto.clinicId, req ? { ip: req.ip, headers: req.headers } : undefined);
  }

  /**
   * GET /auth/me
   *
   * Backend é a fonte única de identidade (single source of truth).
   * Android e Web devem consumir este endpoint para obter a identidade mínima do usuário autenticado.
   */
  @UseGuards(JwtAuthGuard)
  @Get('me')
  async getProfile(@CurrentUser() user: any) {
    return this.authService.validateToken(user.sub);
  }

  /**
   * POST /auth/consent
   *
   * Registra aceite do termo LGPD pelo usuário (conformidade e auditoria).
   * Obrigatório antes de liberar uso completo do app.
   */
  @UseGuards(JwtAuthGuard)
  @Post('consent')
  async recordConsent(@CurrentUser() user: any) {
    return this.authService.recordLgpdConsent(user.sub);
  }
}




