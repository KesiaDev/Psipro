import { Module } from '@nestjs/common';
import { APP_GUARD } from '@nestjs/core';
import { ThrottlerModule, ThrottlerGuard } from '@nestjs/throttler';
import { getSkipIfInternalIp } from './throttler.helper';

/** TTL base: 1 minuto em ms */
const TTL_MS = 60_000;

@Module({
  imports: [
    ThrottlerModule.forRoot({
      throttlers: [
        {
          name: 'default',
          ttl: TTL_MS,
          limit: 100, // Demais rotas
        },
      ],
      skipIf: getSkipIfInternalIp(),
      errorMessage: 'Muitas requisições. Tente novamente em alguns minutos.',
    }),
  ],
  providers: [
    {
      provide: APP_GUARD,
      useClass: ThrottlerGuard,
    },
  ],
})
export class AppThrottlerModule {}
