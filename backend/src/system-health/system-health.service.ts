import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { readFileSync } from 'fs';
import { join } from 'path';
import { PrismaService } from '../prisma/prisma.service';

export type ServiceStatus = 'ok' | 'slow' | 'down';

export interface SystemHealthResult {
  status: 'operational' | 'degraded' | 'down';
  services: {
    backend: string;
    database: string;
    web: string;
    mobileSync: string;
  };
  latency: {
    api: string;
  };
  timestamp: string;
}

export interface ServiceCheckResult {
  status: 'ok' | 'slow' | 'down';
  latencyMs?: number;
}

export interface SystemHealthFullResult {
  status: 'operational' | 'degraded' | 'down';
  version: string;
  uptimeSeconds: number;
  services: {
    backend: ServiceCheckResult;
    database: ServiceCheckResult;
    web: ServiceCheckResult;
    mobileSync: ServiceCheckResult;
  };
  qa: {
    testsPassed: number;
    testsFailed: number;
  };
  latency: {
    totalMs: number;
  };
  timestamp: string;
}

const DEGRADED_MS = 2000; // latência > 2s = degraded

@Injectable()
export class SystemHealthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  async check(): Promise<SystemHealthResult> {
    const startTime = Date.now();

    const services = {
      backend: 'ok' as string,
      database: await this.checkDatabase(),
      web: await this.checkWeb(),
      mobileSync: 'ok' as string,
    };

    // Backend respondeu = ok. mobileSync depende do mesmo backend/DB
    if (services.database === 'ok') {
      services.mobileSync = 'ok';
    } else {
      services.mobileSync = 'down';
    }

    const elapsed = Date.now() - startTime;
    const apiLatencyMs = elapsed;
    const apiLatencyStr = `${apiLatencyMs}ms`;

    const status = this.computeStatus(services, apiLatencyMs);

    return {
      status,
      services,
      latency: { api: apiLatencyStr },
      timestamp: new Date().toISOString(),
    };
  }

  private async checkDatabase(): Promise<string> {
    try {
      const start = Date.now();
      await this.prisma.$queryRaw`SELECT 1`;
      const ms = Date.now() - start;
      return ms > DEGRADED_MS ? 'slow' : 'ok';
    } catch {
      return 'down';
    }
  }

  private async checkWeb(): Promise<string> {
    const dashboardUrl = this.config.get<string>('DASHBOARD_URL');
    if (!dashboardUrl) {
      return 'ok'; // não configurado = assumir ok
    }
    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 5000);
      const res = await fetch(dashboardUrl, {
        method: 'HEAD',
        signal: controller.signal,
      });
      clearTimeout(timeout);
      return res.ok ? 'ok' : 'down';
    } catch {
      return 'down';
    }
  }

  private computeStatus(
    services: Record<string, string>,
    apiLatencyMs: number,
  ): 'operational' | 'degraded' | 'down' {
    const values = Object.values(services);
    if (values.some((v) => v === 'down')) return 'down';
    if (values.some((v) => v === 'slow') || apiLatencyMs > DEGRADED_MS) return 'degraded';
    return 'operational';
  }

  async checkFull(): Promise<SystemHealthFullResult> {
    const startTime = Date.now();

    const backendStart = Date.now();
    const backendResult: ServiceCheckResult = { status: 'ok', latencyMs: 0 };

    const dbResult = await this.checkDatabaseWithLatency();
    const webResult = await this.checkWebWithLatency();

    const mobileSyncResult: ServiceCheckResult =
      dbResult.status === 'ok' ? { status: 'ok' } : { status: 'down' };

    backendResult.latencyMs = Date.now() - backendStart;

    const totalMs = Date.now() - startTime;
    const services = {
      backend: backendResult,
      database: dbResult,
      web: webResult,
      mobileSync: mobileSyncResult,
    };

    const testsPassed = [backendResult, dbResult, webResult, mobileSyncResult].filter(
      (s) => s.status === 'ok',
    ).length;
    const testsFailed = 4 - testsPassed;

    const status = this.computeStatusFromResults(services, totalMs);

    return {
      status,
      version: this.getVersion(),
      uptimeSeconds: Math.floor(process.uptime()),
      services,
      qa: { testsPassed, testsFailed },
      latency: { totalMs },
      timestamp: new Date().toISOString(),
    };
  }

  private async checkDatabaseWithLatency(): Promise<ServiceCheckResult> {
    try {
      const start = Date.now();
      await this.prisma.$queryRaw`SELECT 1`;
      const latencyMs = Date.now() - start;
      const status = latencyMs > DEGRADED_MS ? 'slow' : 'ok';
      return { status, latencyMs };
    } catch {
      return { status: 'down' };
    }
  }

  private async checkWebWithLatency(): Promise<ServiceCheckResult> {
    const dashboardUrl = this.config.get<string>('DASHBOARD_URL');
    if (!dashboardUrl) {
      return { status: 'ok', latencyMs: 0 };
    }
    try {
      const start = Date.now();
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 5000);
      const res = await fetch(dashboardUrl, {
        method: 'HEAD',
        signal: controller.signal,
      });
      clearTimeout(timeout);
      const latencyMs = Date.now() - start;
      const status = !res.ok ? 'down' : latencyMs > DEGRADED_MS ? 'slow' : 'ok';
      return { status, latencyMs };
    } catch {
      return { status: 'down' };
    }
  }

  private computeStatusFromResults(
    services: Record<string, ServiceCheckResult>,
    totalMs: number,
  ): 'operational' | 'degraded' | 'down' {
    const values = Object.values(services);
    if (values.some((v) => v.status === 'down')) return 'down';
    if (values.some((v) => v.status === 'slow') || totalMs > DEGRADED_MS) return 'degraded';
    return 'operational';
  }

  private getVersion(): string {
    try {
      const pkgPath = join(process.cwd(), 'package.json');
      const pkg = JSON.parse(readFileSync(pkgPath, 'utf-8'));
      return (pkg.version as string) ?? '0.0.0';
    } catch {
      return '0.0.0';
    }
  }
}
