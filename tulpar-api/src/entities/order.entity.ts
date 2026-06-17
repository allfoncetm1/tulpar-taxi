import {
  Column,
  CreateDateColumn,
  Entity,
  PrimaryGeneratedColumn,
} from 'typeorm';

export enum OrderStatus {
  NEW = 'new',
  OFFERED = 'offered',
  ACCEPTED = 'accepted',
  ARRIVED = 'arrived',
  IN_PROGRESS = 'in_progress',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled',
}

@Entity('orders')
export class Order {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ name: 'client_id' })
  clientId: string;

  @Column({ name: 'driver_id', nullable: true })
  driverId: string;

  @Column({ name: 'city_id', default: 1 })
  cityId: number;

  @Column({ name: 'from_lat', type: 'decimal', precision: 10, scale: 7 })
  fromLat: number;

  @Column({ name: 'from_lng', type: 'decimal', precision: 10, scale: 7 })
  fromLng: number;

  @Column({ name: 'from_address', type: 'text', nullable: true })
  fromAddress: string;

  @Column({ name: 'to_lat', type: 'decimal', precision: 10, scale: 7 })
  toLat: number;

  @Column({ name: 'to_lng', type: 'decimal', precision: 10, scale: 7 })
  toLng: number;

  @Column({ name: 'to_address', type: 'text', nullable: true })
  toAddress: string;

  @Column({ name: 'estimated_price', type: 'decimal', precision: 10, scale: 2, nullable: true })
  estimatedPrice: number;

  @Column({ name: 'final_price', type: 'decimal', precision: 10, scale: 2, nullable: true })
  finalPrice: number;

  @Column({ type: 'enum', enum: OrderStatus, default: OrderStatus.NEW })
  status: OrderStatus;

  @Column({ nullable: true, type: 'text' })
  comment: string;

  @Column({ name: 'door', nullable: true, type: 'varchar', length: 20 })
  door: string;

  @CreateDateColumn({ name: 'created_at', type: 'timestamptz' })
  createdAt: Date;

  @Column({ name: 'completed_at', type: 'timestamptz', nullable: true })
  completedAt: Date;

  @Column({ nullable: true, type: 'smallint' })
  rating: number;
}
