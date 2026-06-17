import { Column, Entity, PrimaryGeneratedColumn } from 'typeorm';

@Entity('cities')
export class City {
  @PrimaryGeneratedColumn()
  id: number;

  @Column({ name: 'name_kz' })
  nameKz: string;

  @Column({ name: 'name_ru' })
  nameRu: string;

  @Column({ name: 'name_en' })
  nameEn: string;

  @Column({ name: 'region_ru', nullable: true })
  regionRu: string;

  @Column({ type: 'decimal', precision: 10, scale: 7 })
  lat: number;

  @Column({ type: 'decimal', precision: 10, scale: 7 })
  lng: number;

  @Column({ nullable: true })
  viewbox: string;

  @Column({ name: 'is_active', default: true })
  isActive: boolean;

  @Column({ default: 1 })
  version: number;
}
