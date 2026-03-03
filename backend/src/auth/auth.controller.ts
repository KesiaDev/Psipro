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
   * POST /auth/handoff
   *
   * Endpoint seguro de handoff para SSO Android -> Web:
   * - recebe o mesmo JWT já emitido pelo backend (body ou Authorization header)
   * - valida JWT com JwtService (mesma base lógica do /auth/me)
   * - NÃO cria novo token e NÃO altera claims
   * - retorna `{ token, user }` para o Web criar sessão local
   */
  @Post('handoff')
  async handoff(@Body() body: HandoffDto, @Headers('authorization') authorization?: string) {
    const headerToken = authorization?.startsWith('Bearer ')
      ? authorization.slice('Bearer '.length).trim()
      : undefined;

    const token = body?.token || headerToken;
    if (!token) {
      // 401 (ausente) — o Web/Android devem sempre enviar um JWT válido.
      throw new UnauthorizedException('Token inválido');
    }

    return this.authService.handoff(token);
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
}




