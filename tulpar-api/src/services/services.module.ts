import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Driver } from '../entities/driver.entity';
import { Order } from '../entities/order.entity';
import { Tariff } from '../entities/tariff.entity';
import { User } from '../entities/user.entity';
import { ServicesController } from './services.controller';
import { ServicesService } from './services.service';
import { FcmService } from './fcm.service';

@Module({
  imports: [TypeOrmModule.forFeature([Order, Tariff, Driver, User])],
  controllers: [ServicesController],
  providers: [ServicesService, FcmService],
})
export class ServicesModule {}
