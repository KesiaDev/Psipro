import {
  Controller,
  Post,
  Get,
  Body,
  Req,
  Headers,
  UseGuards,
  HttpCode,
  HttpStatus,
  RawBodyRequest,
} from '@nestjs/common';
import { Request } from 'express';
import { SubscriptionsService } from './subscriptions.service';
import { CreateCheckoutDto } from './dto/create-checkout.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { CurrentClinicId } from '../common/decorators/current-clinic.decorator';

@Controller('subscriptions')
export class SubscriptionsController {
  constructor(private readonly subscriptionsService: SubscriptionsService) {}

  @UseGuards(JwtAuthGuard)
  @Post('checkout')
  async createCheckout(
    @CurrentUser() user: any,
    @CurrentClinicId() clinicId: string,
    @Body() dto: CreateCheckoutDto,
  ) {
    return this.subscriptionsService.createCheckoutSession(user.sub, clinicId, dto.planId);
  }

  @UseGuards(JwtAuthGuard)
  @Post('portal')
  async createPortal(@CurrentClinicId() clinicId: string) {
    return this.subscriptionsService.createPortalSession(clinicId);
  }

  @Post('webhook')
  @HttpCode(HttpStatus.OK)
  async handleWebhook(
    @Req() req: RawBodyRequest<Request>,
    @Headers('stripe-signature') signature: string,
  ) {
    const rawBody = req.rawBody;
    if (!rawBody) {
      return { received: false };
    }
    await this.subscriptionsService.handleWebhook(rawBody, signature);
    return { received: true };
  }

  @UseGuards(JwtAuthGuard)
  @Get('me')
  async getMySubscription(@CurrentClinicId() clinicId: string) {
    return this.subscriptionsService.getSubscription(clinicId);
  }
}
