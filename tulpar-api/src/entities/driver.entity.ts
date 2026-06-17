import { Column, Entity, JoinColumn, OneToOne, PrimaryColumn, UpdateDateColumn } from 'typeorm';
import { User } from './user.entity';

export enum DriverStatus {
  OFFLINE = 'offline',
  ONLINE = 'online',
  BUSY = 'busy',
}

@Entity('drivers')
export class Driver {
  @PrimaryColumn({ name: 'user_id' })
  userId: string;

  @OneToOne(() => User)
  @JoinColumn({ name: 'user_id' })
  user: User;

  @Column({ name: 'car_model', nullable: true, length: 100 })
  carModel: string;

  @Column({ name: 'car_number', nullable: true, length: 20 })
  carNumber: string;

  @Column({ name: 'car_color', nullable: true, length: 50 })
  carColor: string;

  @Column({ type: 'decimal', precision: 10, scale: 2, default: 0 })
  balance: number;

  @Column({ type: 'enum', enum: DriverStatus, default: DriverStatus.OFFLINE })
  status: DriverStatus;

  @Column({ name: 'last_lat', type: 'decimal', precision: 10, scale: 7, nullable: true })
  lastLat: number;

  @Column({ name: 'last_lng', type: 'decimal', precision: 10, scale: 7, nullable: true })
  lastLng: number;

  @UpdateDateColumn({ name: 'last_location_at', type: 'timestamptz', nullable: true })
  lastLocationAt: Date;
}
