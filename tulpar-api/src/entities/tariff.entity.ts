import { Column, Entity, PrimaryGeneratedColumn } from 'typeorm';

@Entity('tariffs')
export class Tariff {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'city_id' })
  cityId: number;

  @Column({ length: 100 })
  name: string;

  @Column({ name: 'base_price', type: 'decimal', precision: 10, scale: 2 })
  basePrice: number;

  @Column({ name: 'price_per_km', type: 'decimal', precision: 10, scale: 2 })
  pricePerKm: number;

  @Column({ name: 'min_price', type: 'decimal', precision: 10, scale: 2 })
  minPrice: number;

  @Column({ name: 'is_active', default: true })
  isActive: boolean;
}
