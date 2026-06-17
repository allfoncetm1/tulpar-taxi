import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { MoreThan, Repository } from 'typeorm';
import { Address } from '../entities/address.entity';
import { City } from '../entities/city.entity';
import { Driver } from '../entities/driver.entity';
import { Tariff } from '../entities/tariff.entity';
import { User } from '../entities/user.entity';

@Injectable()
export class ClientService {
  constructor(
    @InjectRepository(User) private readonly userRepo: Repository<User>,
    @InjectRepository(City) private readonly cityRepo: Repository<City>,
    @InjectRepository(Address) private readonly addressRepo: Repository<Address>,
    @InjectRepository(Tariff) private readonly tariffRepo: Repository<Tariff>,
    @InjectRepository(Driver) private readonly driverRepo: Repository<Driver>,
  ) {}

  async getStartData(userId: string) {
    const user = await this.userRepo.findOne({ where: { id: userId } });
    if (!user) throw new NotFoundException('Пользователь не найден');

    const city = await this.cityRepo.findOne({ where: { id: user.cityId ?? 1 } });
    const tariffs = await this.tariffRepo.find({ where: { cityId: city?.id ?? 1, isActive: true } });

    return {
      user: {
        id: user.id,
        phone: user.phone,
        name: user.name,
        isDriver: user.isDriver,
        cityId: user.cityId,
      },
      city,
      tariffs,
    };
  }

  async getCity(cityId: number) {
    const city = await this.cityRepo.findOne({ where: { id: cityId } });
    if (!city) throw new NotFoundException('Город не найден');
    const tariffs = await this.tariffRepo.find({ where: { cityId, isActive: true } });
    return { city, tariffs };
  }

  async loadCities() {
    return this.cityRepo.find({ where: { isActive: true } });
  }

  async loadAddresses(cityId: number, version: number) {
    const addresses = await this.addressRepo.find({
      where: { cityId, version: MoreThan(version), isDeleted: false },
    });
    return { addresses, version: addresses.length ? Math.max(...addresses.map(a => a.version)) : version };
  }

  async updateLocation(userId: string, lat: number, lng: number, isDriver: boolean) {
    if (isDriver) {
      const driver = await this.driverRepo.findOne({ where: { userId } });
      if (driver) {
        driver.lastLat = lat;
        driver.lastLng = lng;
        await this.driverRepo.save(driver);
      }
    }
    return { success: true };
  }

  async checkAppStatus(os: string, version: string) {
    return { needUpdate: false, minVersion: '1.0.0', currentVersion: '1.0.0' };
  }
}
