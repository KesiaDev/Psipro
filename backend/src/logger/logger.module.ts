import { Global, Module } from '@nestjs/common';
import { APP_FILTER, APP_INTERCEPTOR } from '@nestjs/core';
import { LoggerService } from './logger.service';
import { LoggingInterceptor } from './logging.interceptor';
import { LoggingExceptionFilter } from './logging-exception.filter';

@Global()
@Module({
  providers: [
    LoggerService,
    { provide: APP_FILTER, useClass: LoggingExceptionFilter },
    { provide: APP_INTERCEPTOR, useClass: LoggingInterceptor },
  ],
  exports: [LoggerService],
})
export class LoggerModule {}
