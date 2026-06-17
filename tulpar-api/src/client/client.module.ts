import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Address } from '../entities/address.entity';
import { City } from '../entities/city.entity';
import { Driver } from '../entities/driver.entity';
import { Tariff } from '../entities/tariff.entity';
import { User } from '../entities/user.entity';
import { ClientController } from './client.controller';
import { ClientService } from './client.service';

@Module({
  imports: [TypeOrmModule.forFeature([User, City, Address, Tariff, Driver])],
  controllers: [ClientController],
  providers: [ClientService],
})
export class ClientModule {}
