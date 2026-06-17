import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { AuthModule } from './auth/auth.module';
import { ClientModule } from './client/client.module';
import { DatabaseModule } from './database/database.module';
import { Address } from './entities/address.entity';
import { City } from './entities/city.entity';
import { Driver } from './entities/driver.entity';
import { Order } from './entities/order.entity';
import { Tariff } from './entities/tariff.entity';
import { User } from './entities/user.entity';
import { ServicesModule } from './services/services.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    TypeOrmModule.forRoot(
      process.env.DATABASE_URL
        ? {
            type: 'postgres',
            url: process.env.DATABASE_URL,
            ssl: { rejectUnauthorized: false },
            entities: [User, City, Address, Order, Tariff, Driver],
            synchronize: true,
            logging: false,
          }
        : {
            type: 'postgres',
            host: process.env.DB_HOST ?? 'localhost',
            port: +(process.env.DB_PORT ?? 5432),
            database: process.env.DB_NAME ?? 'tulpar_taxi',
            username: process.env.DB_USER ?? 'postgres',
            password: process.env.DB_PASS ?? 'postgres',
            entities: [User, City, Address, Order, Tariff, Driver],
            synchronize: process.env.NODE_ENV !== 'production',
            logging: process.env.NODE_ENV === 'development',
          },
    ),
    AuthModule,
    ClientModule,
    ServicesModule,
    DatabaseModule,
  ],
})
export class AppModule {}
