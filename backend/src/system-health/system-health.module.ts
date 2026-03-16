import { Module } from '@nestjs/common';
import { APP_INTERCEPTOR } from '@nestjs/core';
import { SystemHealthController } from './system-health.controller';
import { SystemHealthService } from './system-health.service';
import { MetricsService } from './metrics.service';
import { MetricsInterceptor } from './metrics.interceptor';

@Module({
  controllers: [SystemHealthController],
  providers: [
    SystemHealthService,
    MetricsService,
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    { provide: APP_INTERCEPTOR, useClass: MetricsInterceptor, multi: true } as any,
  ],
})
export class SystemHealthModule {}
