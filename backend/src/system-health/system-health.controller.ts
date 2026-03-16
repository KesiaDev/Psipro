import { Controller, Get } from '@nestjs/common';
import { SystemHealthService } from './system-health.service';
import { MetricsService } from './metrics.service';

@Controller('system-health')
export class SystemHealthController {
  constructor(
    private readonly systemHealthService: SystemHealthService,
    private readonly metricsService: MetricsService,
  ) {}

  @Get()
  async getHealth() {
    return this.systemHealthService.check();
  }

  @Get('full')
  async getHealthFull() {
    return this.systemHealthService.checkFull();
  }

  @Get('metrics')
  async getMetrics() {
    return this.metricsService.getMetrics();
  }
}
