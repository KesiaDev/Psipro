import { Controller, Post, Body, UseGuards, Get, Headers, UnauthorizedException } from '@nestjs/common';
import { AuthService } from './auth.service';
import { LoginDto } from './dto/login.dto';
import { HandoffDto } from './dto/handoff.dto';
import { RegisterDto } from './dto/register.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';

@Controller('auth')
export class AuthController {
  constructor(private authService: AuthService) {}

  @Post('login')
  async login(@Body() loginDto: LoginDto) {
    return this.authService.login(loginDto);
  }

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




