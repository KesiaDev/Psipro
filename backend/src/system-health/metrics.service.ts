import { Injectable } from '@nestjs/common';

export interface MetricsResult {
  requestsPerMinute: number;
  errorRate: number;
  avgLatencyMs: number;
  activeUsers: number;
}

@Injectable()
export class MetricsService {
  private currentMinute = '';
  private requestsThisMinute = 0;
  private errorsThisMinute = 0;
  private totalLatencyThisMinute = 0;
  private activeUserIds = new Set<string>();

  recordRequest(latencyMs: number, isError: boolean, clientId: string): void {
    this.resetIfNewMinute();

    this.requestsThisMinute += 1;
    if (isError) this.errorsThisMinute += 1;
    this.totalLatencyThisMinute += latencyMs;
    this.activeUserIds.add(clientId);
  }

  getMetrics(): MetricsResult {
    this.resetIfNewMinute();

    const errorRate =
      this.requestsThisMinute > 0
        ? this.errorsThisMinute / this.requestsThisMinute
        : 0;

    const avgLatencyMs =
      this.requestsThisMinute > 0
        ? Math.round(this.totalLatencyThisMinute / this.requestsThisMinute)
        : 0;

    return {
      requestsPerMinute: this.requestsThisMinute,
      errorRate: Math.round(errorRate * 1000) / 1000,
      avgLatencyMs,
      activeUsers: this.activeUserIds.size,
    };
  }

  private resetIfNewMinute(): void {
    const now = new Date();
    const minuteKey = `${now.getUTCFullYear()}-${String(now.getUTCMonth() + 1).padStart(2, '0')}-${String(now.getUTCDate()).padStart(2, '0')}T${String(now.getUTCHours()).padStart(2, '0')}:${String(now.getUTCMinutes()).padStart(2, '0')}`;

    if (this.currentMinute !== minuteKey) {
      this.currentMinute = minuteKey;
      this.requestsThisMinute = 0;
      this.errorsThisMinute = 0;
      this.totalLatencyThisMinute = 0;
      this.activeUserIds.clear();
    }
  }
}
