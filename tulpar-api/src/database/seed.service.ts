import { Injectable, Logger, OnApplicationBootstrap } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { City } from '../entities/city.entity';
import { Tariff } from '../entities/tariff.entity';

@Injectable()
export class SeedService implements OnApplicationBootstrap {
  private readonly logger = new Logger(SeedService.name);

  constructor(
    @InjectRepository(City) private readonly cityRepo: Repository<City>,
    @InjectRepository(Tariff) private readonly tariffRepo: Repository<Tariff>,
  ) {}

  async onApplicationBootstrap() {
    await this.seedCity();
    await this.seedTariff();
  }

  private async seedCity() {
    const exists = await this.cityRepo.findOne({ where: { id: 1 } });
    if (exists) return;

    await this.cityRepo.save({
      id: 1,
      nameKz: 'Қандыағаш',
      nameRu: 'Кандыагаш',
      nameEn: 'Kandyagash',
      regionRu: 'Актюбинская область',
      lat: 49.46504,
      lng: 57.410118,
      viewbox: '49.42,57.35,49.51,57.47',
      isActive: true,
      version: 1,
    });
    this.logger.log('Seed: город Кандыагаш добавлен');
  }

  private async seedTariff() {
    const exists = await this.tariffRepo.findOne({ where: { cityId: 1 } });
    if (exists) return;

    await this.tariffRepo.save({
      cityId: 1,
      name: 'Стандарт',
      basePrice: 500,
      pricePerKm: 150,
      minPrice: 500,
      isActive: true,
    });
    this.logger.log('Seed: тариф Кандыагаш добавлен');
  }
}
