import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class InsightsService {
  constructor(private prisma: PrismaService) {}

  async findAll(userId: string) {
    return this.prisma.insight.findMany({
      where: {
        userId,
        dismissed: false,
      },
      orderBy: { createdAt: 'desc' },
      take: 10,
    });
  }

  async dismiss(id: string, userId: string) {
    return this.prisma.insight.updateMany({
      where: {
        id,
        userId,
      },
      data: {
        dismissed: true,
      },
    });
  }
}

