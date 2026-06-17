import { Body, Controller, Post, Request, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { ClientService } from './client.service';

@ApiTags('client')
@ApiBearerAuth()
@UseGuards(AuthGuard('jwt'))
@Controller('api/client')
export class ClientController {
  constructor(private readonly clientService: ClientService) {}

  @Post('GetStartData')
  @ApiOperation({ summary: 'Стартовые данные: пользователь, город, тарифы' })
  getStartData(@Request() req) {
    return this.clientService.getStartData(req.user.id);
  }

  @Post('GetCity')
  @ApiOperation({ summary: 'Данные города по ID' })
  getCity(@Body() body: { cityId: number }) {
    return this.clientService.getCity(body.cityId ?? 1);
  }

  @Post('LoadCities')
  @ApiOperation({ summary: 'Список активных городов' })
  loadCities() {
    return this.clientService.loadCities();
  }

  @Post('LoadAddresses')
  @ApiOperation({ summary: 'Дельта адресов с версии (для Room-кэша)' })
  loadAddresses(@Body() body: { cityId: number; version: number }) {
    return this.clientService.loadAddresses(body.cityId ?? 1, body.version ?? 0);
  }

  @Post('UpdateLocation')
  @ApiOperation({ summary: 'Обновить GPS-координаты' })
  updateLocation(
    @Request() req,
    @Body() body: { lat: number; lng: number; isDriver: boolean },
  ) {
    return this.clientService.updateLocation(req.user.id, body.lat, body.lng, body.isDriver);
  }

  @Post('CheckAppStatus')
  @ApiOperation({ summary: 'Проверить версию приложения' })
  checkAppStatus(@Body() body: { os: string; version: string }) {
    return this.clientService.checkAppStatus(body.os, body.version);
  }
}
