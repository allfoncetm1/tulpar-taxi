import { Column, Entity, PrimaryColumn } from 'typeorm';

@Entity('addresses')
export class Address {
  @PrimaryColumn()
  id: string;

  @Column({ name: 'city_id' })
  cityId: number;

  @Column({ name: 'city_section_id', nullable: true })
  citySectionId: number;

  @Column({ name: 'name_kz', type: 'text' })
  nameKz: string;

  @Column({ name: 'name_ru', type: 'text' })
  nameRu: string;

  @Column({ name: 'name_en', type: 'text' })
  nameEn: string;

  @Column({ type: 'decimal', precision: 10, scale: 7 })
  lat: number;

  @Column({ type: 'decimal', precision: 10, scale: 7 })
  lng: number;

  @Column({ nullable: true, type: 'text' })
  tags: string;

  @Column({ nullable: true })
  radius: number;

  @Column({ default: 1 })
  version: number;

  @Column({ name: 'is_deleted', default: false })
  isDeleted: boolean;

  @Column({ name: 'updated_at', type: 'timestamptz' })
  updatedAt: Date;
}
