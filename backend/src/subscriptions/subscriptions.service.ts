import { Injectable, BadRequestException, NotFoundException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class SubscriptionsService {
  constructor(
    private prisma: PrismaService,
    private config: ConfigService,
  ) {}

  private getStripe() {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const Stripe = require('stripe');
    const secretKey = this.config.get<string>('STRIPE_SECRET_KEY');
    if (!secretKey) throw new BadRequestException('Stripe não configurado');
    return new Stripe(secretKey, { apiVersion: '2024-06-20' });
  }

  private getPriceId(planId: string): string {
    const map: Record<string, string> = {
      starter: this.config.get<string>('STRIPE_PRICE_STARTER') || '',
      pro: this.config.get<string>('STRIPE_PRICE_PRO') || '',
      enterprise: this.config.get<string>('STRIPE_PRICE_ENTERPRISE') || '',
    };
    const priceId = map[planId];
    if (!priceId) throw new BadRequestException(`Plano inválido: ${planId}`);
    return priceId;
  }

  async createCheckoutSession(userId: string, clinicId: string, planId: string): Promise<{ url: string }> {
    const stripe = this.getStripe();
    const frontendUrl = this.config.get<string>('FRONTEND_URL') || 'https://psipro-dashboard-production.up.railway.app';

    const user = await this.prisma.user.findUnique({ where: { id: userId }, select: { email: true } });
    if (!user) throw new NotFoundException('Usuário não encontrado');

    const subscription = await this.prisma.subscription.findFirst({ where: { clinicId } });

    const session = await stripe.checkout.sessions.create({
      mode: 'subscription',
      customer_email: subscription?.stripeCustomerId ? undefined : user.email,
      customer: subscription?.stripeCustomerId || undefined,
      line_items: [{ price: this.getPriceId(planId), quantity: 1 }],
      success_url: `${frontendUrl}/billing?success=true&plan=${planId}`,
      cancel_url: `${frontendUrl}/billing?canceled=true`,
      metadata: { userId, clinicId, planId },
    });

    return { url: session.url! };
  }

  async createPortalSession(clinicId: string): Promise<{ url: string }> {
    const stripe = this.getStripe();
    const frontendUrl = this.config.get<string>('FRONTEND_URL') || 'https://psipro-dashboard-production.up.railway.app';

    const subscription = await this.prisma.subscription.findFirst({ where: { clinicId } });
    if (!subscription?.stripeCustomerId) {
      throw new BadRequestException('Nenhuma assinatura ativa encontrada');
    }

    const session = await stripe.billingPortal.sessions.create({
      customer: subscription.stripeCustomerId,
      return_url: `${frontendUrl}/billing`,
    });

    return { url: session.url };
  }

  async handleWebhook(rawBody: Buffer, signature: string): Promise<void> {
    const stripe = this.getStripe();
    const webhookSecret = this.config.get<string>('STRIPE_WEBHOOK_SECRET');
    if (!webhookSecret) throw new BadRequestException('Webhook secret não configurado');

    let event: any;
    try {
      event = stripe.webhooks.constructEvent(rawBody, signature, webhookSecret);
    } catch (err) {
      throw new BadRequestException(`Webhook inválido: ${(err as Error).message}`);
    }

    if (event.type === 'checkout.session.completed') {
      const session = event.data.object;
      const { clinicId, planId } = session.metadata || {};
      if (!clinicId) return;

      await this.prisma.subscription.upsert({
        where: { clinicId },
        create: {
          clinicId,
          stripeCustomerId: session.customer,
          stripeSubscriptionId: session.subscription,
          status: 'ACTIVE',
        },
        update: {
          stripeCustomerId: session.customer,
          stripeSubscriptionId: session.subscription,
          status: 'ACTIVE',
        },
      });
    }

    if (event.type === 'customer.subscription.deleted' || event.type === 'customer.subscription.updated') {
      const sub = event.data.object;
      const existing = await this.prisma.subscription.findFirst({
        where: { stripeSubscriptionId: sub.id },
      });
      if (!existing) return;

      const status = sub.status === 'active' ? 'ACTIVE' : sub.status === 'past_due' ? 'PAST_DUE' : 'CANCELED';
      await this.prisma.subscription.update({
        where: { id: existing.id },
        data: {
          status,
          currentPeriodEnd: new Date(sub.current_period_end * 1000),
        },
      });
    }
  }

  async getSubscription(clinicId: string) {
    const subscription = await this.prisma.subscription.findFirst({
      where: { clinicId },
    });
    return subscription ?? { status: 'NONE', clinicId };
  }
}
