import {
  Column,
  CreateDateColumn,
  Entity,
  PrimaryGeneratedColumn,
} from 'typeorm';

@Entity('users')
export class User {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ unique: true, length: 20 })
  phone: string;

  @Column({ nullable: true, length: 100 })
  name: string;

  @Column({ name: 'is_driver', default: false })
  isDriver: boolean;

  @Column({ name: 'city_id', nullable: true })
  cityId: number;

  @Column({ name: 'auth_token', nullable: true, length: 512 })
  authToken: string;

  @Column({ name: 'firebase_token', nullable: true, type: 'text' })
  firebaseToken: string;

  @Column({ name: 'sms_code', nullable: true, length: 10, type: 'varchar' })
  smsCode: string;

  @Column({ name: 'sms_code_expires_at', nullable: true, type: 'timestamptz' })
  smsCodeExpiresAt: Date;

  @CreateDateColumn({ name: 'created_at', type: 'timestamptz' })
  createdAt: Date;
}
