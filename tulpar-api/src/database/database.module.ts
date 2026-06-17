import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { City } from '../entities/city.entity';
import { Tariff } from '../entities/tariff.entity';
import { SeedService } from './seed.service';

@Module({
  imports: [TypeOrmModule.forFeature([City, Tariff])],
  providers: [SeedService],
})
export class DatabaseModule {}
