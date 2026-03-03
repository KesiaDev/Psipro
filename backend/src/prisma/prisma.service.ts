import { Injectable, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { PrismaClient } from '@prisma/client';

const SOFT_DELETE_MODELS = ['patient', 'appointment', 'session', 'payment'];

@Injectable()
export class PrismaService extends PrismaClient implements OnModuleInit, OnModuleDestroy {
  constructor() {
    super();
    this.$use(async (params, next) => {
      if (params.action === 'delete' || params.action === 'deleteMany') {
        const model = params.model?.toLowerCase();
        if (model && SOFT_DELETE_MODELS.includes(model) && process.env.NODE_ENV === 'production') {
          throw new Error(`Hard delete de ${model} bloqueado em produção. Use soft delete.`);
        }
      }
      return next(params);
    });
  }

  async onModuleInit() {
    await this.$connect();
  }

  async onModuleDestroy() {
    await this.$disconnect();
  }
}




