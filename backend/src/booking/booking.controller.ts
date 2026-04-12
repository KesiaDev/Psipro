import { Body, Controller, Get, Post, Query } from '@nestjs/common';
import { BookingService } from './booking.service';

@Controller('public/booking')
export class BookingController {
  constructor(private readonly bookingService: BookingService) {}

  /** GET /api/public/booking/slots?date=YYYY-MM-DD&duration=60 */
  @Get('slots')
  getSlots(
    @Query('date') date: string,
    @Query('duration') duration?: string,
  ) {
    return this.bookingService.getPublicSlots(
      date,
      duration ? parseInt(duration, 10) : 60,
    );
  }

  /** GET /api/public/booking/available-days?year=2026&month=4 */
  @Get('available-days')
  getAvailableDays(
    @Query('year') year: string,
    @Query('month') month: string,
  ) {
    return this.bookingService.getAvailableDays(
      parseInt(year, 10),
      parseInt(month, 10),
    );
  }

  /** POST /api/public/booking */
  @Post()
  createBooking(
    @Body()
    body: {
      patientName: string;
      patientEmail?: string;
      patientPhone?: string;
      isoDateTime: string;
      notes?: string;
      durationMinutes?: number;
    },
  ) {
    return this.bookingService.createPublicBooking(body);
  }
}
