import { Body, Controller, Post, Request, UseGuards } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { ServicesService } from './services.service';

@ApiTags('services')
@ApiBearerAuth()
@UseGuards(AuthGuard('jwt'))
@Controller('api/services')
export class ServicesController {
  constructor(private readonly svc: ServicesService) {}

  @Post('Estimate')
  @ApiOperation({ summary: 'Рассчитать стоимость поездки' })
  estimate(@Body() body: { fromLat: number; fromLng: number; toLat: number; toLng: number; cityId?: number }) {
    return this.svc.estimate(body.fromLat, body.fromLng, body.toLat, body.toLng, body.cityId);
  }

  @Post('createNewCustom')
  @ApiOperation({ summary: 'Создать новый заказ' })
  createNewCustom(@Request() req, @Body() body: any) {
    return this.svc.createNewCustom(req.user.id, body);
  }

  @Post('getNewCustoms')
  @ApiOperation({ summary: 'Активные заказы (для водителя)' })
  getNewCustoms(@Request() req) {
    return this.svc.getNewCustoms(req.user.id);
  }

  @Post('AcceptOrder')
  @ApiOperation({ summary: 'Принять заказ (водитель)' })
  acceptOrder(@Request() req, @Body() body: { orderId: string }) {
    return this.svc.acceptOrder(req.user.id, body.orderId);
  }

  @Post('SetAction')
  @ApiOperation({ summary: 'Сменить статус поездки: arrived / start / complete / cancel' })
  setAction(@Request() req, @Body() body: { orderId: string; action: string }) {
    return this.svc.setAction(req.user.id, body.orderId, body.action);
  }

  @Post('getHistoryByClient')
  @ApiOperation({ summary: 'История поездок клиента' })
  getHistoryByClient(@Request() req) {
    return this.svc.getHistoryByClient(req.user.id);
  }

  @Post('getHistoryByDriver')
  @ApiOperation({ summary: 'История поездок водителя' })
  getHistoryByDriver(@Request() req) {
    return this.svc.getHistoryByDriver(req.user.id);
  }

  @Post('GetBalance')
  @ApiOperation({ summary: 'Баланс водителя' })
  getBalance(@Request() req) {
    return this.svc.getBalance(req.user.id);
  }

  @Post('getDriverInfo')
  @ApiOperation({ summary: 'Профиль водителя' })
  getDriverInfo(@Request() req) {
    return this.svc.getDriverInfo(req.user.id);
  }

  @Post('saveDriverInfo')
  @ApiOperation({ summary: 'Сохранить профиль водителя' })
  saveDriverInfo(@Request() req, @Body() body: any) {
    return this.svc.saveDriverInfo(req.user.id, body);
  }

  @Post('UpdateDeviceId')
  @ApiOperation({ summary: 'Обновить FCM-токен устройства' })
  updateDeviceId(@Request() req, @Body() body: { firebaseToken: string }) {
    return this.svc.updateDeviceId(req.user.id, body.firebaseToken);
  }

  @Post('changeMyName')
  @ApiOperation({ summary: 'Изменить имя пользователя' })
  changeMyName(@Request() req, @Body() body: { name: string }) {
    return this.svc.changeMyName(req.user.id, body.name);
  }

  @Post('RateOrder')
  @ApiOperation({ summary: 'Оценить поездку (1-5 звёзд)' })
  rateOrder(@Request() req, @Body() body: { orderId: string; rating: number }) {
    return this.svc.rateOrder(req.user.id, body.orderId, body.rating);
  }

  @Post('GetMyActiveOrder')
  @ApiOperation({ summary: 'Текущий активный заказ клиента (со статусом и инфой водителя)' })
  getMyActiveOrder(@Request() req) {
    return this.svc.getMyActiveOrder(req.user.id);
  }

  @Post('CancelMyOrder')
  @ApiOperation({ summary: 'Отменить свой активный заказ (клиент)' })
  cancelMyOrder(@Request() req, @Body() body: { orderId: string }) {
    return this.svc.cancelMyOrder(req.user.id, body.orderId);
  }

  @Post('checkPoint')
  @ApiOperation({ summary: 'Проверить, находится ли точка в зоне города' })
  checkPoint(@Body() body: { lat: number; lng: number }) {
    return this.svc.checkPoint(body.lat, body.lng);
  }
}
