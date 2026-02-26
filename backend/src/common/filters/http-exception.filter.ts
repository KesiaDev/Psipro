import {
  ExceptionFilter,
  Catch,
  ArgumentsHost,
  HttpException,
  HttpStatus,
  Logger,
} from '@nestjs/common';
import { Request, Response } from 'express';

@Catch()
export class AllExceptionsFilter implements ExceptionFilter {
  private readonly logger = new Logger(AllExceptionsFilter.name);

  catch(exception: unknown, host: ArgumentsHost): void {
    const ctx = host.switchToHttp();
    const response = ctx.getResponse<Response>();
    const request = ctx.getRequest<Request>();

    const status =
      exception instanceof HttpException
        ? exception.getStatus()
        : HttpStatus.INTERNAL_SERVER_ERROR;

    const rawMessage =
      exception instanceof HttpException
        ? exception.getResponse()
        : exception instanceof Error
          ? exception.message
          : 'Internal server error';

    const message =
      typeof rawMessage === 'object' && rawMessage !== null && 'message' in rawMessage
        ? (rawMessage as { message: string | string[] }).message
        : typeof rawMessage === 'string'
          ? rawMessage
          : 'Internal server error';

    const clientMessage = Array.isArray(message) ? message[0] : message;

    const isProduction = process.env.NODE_ENV === 'production';

    if (!isProduction && exception instanceof Error && exception.stack) {
      this.logger.error(
        JSON.stringify({
          statusCode: status,
          path: request.url,
          method: request.method,
          timestamp: new Date().toISOString(),
          error: exception.message,
          stack: exception.stack,
        }),
      );
    } else {
      this.logger.error(
        JSON.stringify({
          statusCode: status,
          path: request.url,
          method: request.method,
          timestamp: new Date().toISOString(),
          error: exception instanceof Error ? exception.message : String(exception),
        }),
      );
    }

    const body = {
      statusCode: status,
      message: clientMessage,
      timestamp: new Date().toISOString(),
      path: request.url,
    };

    response.status(status).json(body);
  }
}
