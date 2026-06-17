import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Driver, DriverStatus } from '../entities/driver.entity';
import { Order, OrderStatus } from '../entities/order.entity';
import { Tariff } from '../entities/tariff.entity';
import { User } from '../entities/user.entity';
import { FcmService } from './fcm.service';

const KANDYAGASH_LAT = 49.46504;
const KANDYAGASH_LNG = 57.410118;
const VIEWBOX = { minLat: 49.42, minLng: 57.35, maxLat: 49.51, maxLng: 57.47 };

@Injectable()
export class ServicesService {
  constructor(
    @InjectRepository(Order) private readonly orderRepo: Repository<Order>,
    @InjectRepository(Tariff) private readonly tariffRepo: Repository<Tariff>,
    @InjectRepository(Driver) private readonly driverRepo: Repository<Driver>,
    @InjectRepository(User) private readonly userRepo: Repository<User>,
    private readonly fcm: FcmService,
  ) {}

  async estimate(fromLat: number, fromLng: number, toLat: number, toLng: number, cityId = 1) {
    this.validatePoint(fromLat, fromLng);
    this.validatePoint(toLat, toLng);

    const distanceKm = this.haversine(fromLat, fromLng, toLat, toLng);
    const tariff = await this.tariffRepo.findOne({ where: { cityId, isActive: true } });

    const basePrice = +(tariff?.basePrice ?? 500);
    const pricePerKm = +(tariff?.pricePerKm ?? 150);
    const minPrice = +(tariff?.minPrice ?? 500);

    const price = Math.max(minPrice, basePrice + distanceKm * pricePerKm);

    return {
      distanceKm: +distanceKm.toFixed(2),
      estimatedPrice: Math.round(price),
      currency: 'KZT',
      tariffName: tariff?.name ?? 'Стандарт',
    };
  }

  async createNewCustom(clientId: string, body: {
    fromLat: number; fromLng: number; fromAddress: string;
    toLat: number; toLng: number; toAddress: string;
    cityId?: number; comment?: string; door?: string;
  }) {
    const est = await this.estimate(body.fromLat, body.fromLng, body.toLat, body.toLng, body.cityId);

    const order = this.orderRepo.create({
      clientId,
      cityId: body.cityId ?? 1,
      fromLat: body.fromLat,
      fromLng: body.fromLng,
      fromAddress: body.fromAddress,
      toLat: body.toLat,
      toLng: body.toLng,
      toAddress: body.toAddress,
      estimatedPrice: est.estimatedPrice,
      comment: body.comment,
      door: body.door,
      status: OrderStatus.NEW,
    });

    await this.orderRepo.save(order);
    return { orderId: order.id, status: order.status, estimatedPrice: order.estimatedPrice };
  }

  async getNewCustoms(driverId: string) {
    return this.orderRepo.find({
      where: { status: OrderStatus.NEW },
      order: { createdAt: 'DESC' },
      take: 20,
    });
  }

  async acceptOrder(driverId: string, orderId: string) {
    const order = await this.findOrder(orderId);
    if (order.status !== OrderStatus.NEW) throw new BadRequestException('Заказ уже принят');
    order.driverId = driverId;
    order.status = OrderStatus.ACCEPTED;
    await this.orderRepo.save(order);

    const driver = await this.driverRepo.findOne({ where: { userId: driverId } });
    if (driver) { driver.status = DriverStatus.BUSY; await this.driverRepo.save(driver); }

    const client = await this.userRepo.findOne({ where: { id: order.clientId } });
    if (client?.firebaseToken) {
      await this.fcm.sendToToken(client.firebaseToken, 'Водитель едет!', 'Ваш заказ принят. Водитель уже в пути.');
    }

    return { success: true, orderId };
  }

  async setAction(userId: string, orderId: string, action: string) {
    const order = await this.findOrder(orderId);
    const statusMap: Record<string, OrderStatus> = {
      arrived: OrderStatus.ARRIVED,
      start: OrderStatus.IN_PROGRESS,
      complete: OrderStatus.COMPLETED,
      cancel: OrderStatus.CANCELLED,
    };
    const newStatus = statusMap[action];
    if (!newStatus) throw new BadRequestException('Неизвестный action');

    order.status = newStatus;
    if (newStatus === OrderStatus.COMPLETED) {
      order.completedAt = new Date();
      order.finalPrice = order.estimatedPrice;
      const driver = await this.driverRepo.findOne({ where: { userId } });
      if (driver) { driver.status = DriverStatus.ONLINE; await this.driverRepo.save(driver); }
    }
    if (newStatus === OrderStatus.CANCELLED) {
      const driver = await this.driverRepo.findOne({ where: { userId } });
      if (driver) { driver.status = DriverStatus.ONLINE; await this.driverRepo.save(driver); }
    }

    await this.orderRepo.save(order);

    const pushMessages: Record<string, { title: string; body: string }> = {
      arrived: { title: 'Водитель прибыл!', body: 'Водитель ждёт вас.' },
      complete: { title: 'Поездка завершена', body: 'Спасибо что воспользовались Tulpar Taxi!' },
      cancel: { title: 'Заказ отменён', body: 'Водитель отменил заказ. Попробуйте заказать снова.' },
    };
    const push = pushMessages[action];
    if (push) {
      const client = await this.userRepo.findOne({ where: { id: order.clientId } });
      if (client?.firebaseToken) {
        await this.fcm.sendToToken(client.firebaseToken, push.title, push.body);
      }
    }

    return { success: true, status: order.status };
  }

  async getHistoryByClient(clientId: string) {
    return this.orderRepo.find({
      where: { clientId },
      order: { createdAt: 'DESC' },
      take: 50,
    });
  }

  async getHistoryByDriver(driverId: string) {
    return this.orderRepo.find({
      where: { driverId },
      order: { createdAt: 'DESC' },
      take: 50,
    });
  }

  async getBalance(driverId: string) {
    const driver = await this.driverRepo.findOne({ where: { userId: driverId } });
    return { balance: driver?.balance ?? 0, currency: 'KZT' };
  }

  async getDriverInfo(driverId: string) {
    const driver = await this.driverRepo.findOne({ where: { userId: driverId } });
    if (!driver) throw new NotFoundException('Водитель не найден');
    return driver;
  }

  async saveDriverInfo(driverId: string, data: Partial<Driver>) {
    let driver = await this.driverRepo.findOne({ where: { userId: driverId } });
    if (!driver) {
      driver = this.driverRepo.create({ userId: driverId });
    }
    Object.assign(driver, data);
    await this.driverRepo.save(driver);
    return { success: true };
  }

  async updateDeviceId(userId: string, firebaseToken: string) {
    await this.userRepo.update(userId, { firebaseToken });
    return { success: true };
  }

  async changeMyName(userId: string, name: string) {
    await this.userRepo.update(userId, { name });
    return { success: true };
  }

  async rateOrder(clientId: string, orderId: string, rating: number) {
    if (rating < 1 || rating > 5) throw new BadRequestException('Оценка должна быть от 1 до 5');
    const order = await this.findOrder(orderId);
    if (order.clientId !== clientId) throw new BadRequestException('Это не ваш заказ');
    if (order.status !== OrderStatus.COMPLETED) throw new BadRequestException('Заказ ещё не завершён');
    order.rating = rating;
    await this.orderRepo.save(order);
    return { success: true };
  }

  async checkPoint(lat: number, lng: number) {
    const inCity =
      lat >= VIEWBOX.minLat && lat <= VIEWBOX.maxLat &&
      lng >= VIEWBOX.minLng && lng <= VIEWBOX.maxLng;
    return { inCity, cityId: inCity ? 1 : null };
  }

  private async findOrder(orderId: string): Promise<Order> {
    const order = await this.orderRepo.findOne({ where: { id: orderId } });
    if (!order) throw new NotFoundException('Заказ не найден');
    return order;
  }

  private validatePoint(lat: number, lng: number) {
    if (
      lat < VIEWBOX.minLat || lat > VIEWBOX.maxLat ||
      lng < VIEWBOX.minLng || lng > VIEWBOX.maxLng
    ) {
      throw new BadRequestException('Точка вне зоны обслуживания Кандыагаша');
    }
  }

  private haversine(lat1: number, lng1: number, lat2: number, lng2: number): number {
    const R = 6371;
    const dLat = this.toRad(lat2 - lat1);
    const dLng = this.toRad(lng2 - lng1);
    const a =
      Math.sin(dLat / 2) ** 2 +
      Math.cos(this.toRad(lat1)) * Math.cos(this.toRad(lat2)) * Math.sin(dLng / 2) ** 2;
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  }

  private toRad(deg: number) { return (deg * Math.PI) / 180; }
}
