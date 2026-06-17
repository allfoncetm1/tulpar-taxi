import {
  BadRequestException,
  Injectable,
  UnauthorizedException,
} from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../entities/user.entity';
import { RegisterV2Dto, RegisterWithTokenDto, SendSmsDto } from './dto/register.dto';

@Injectable()
export class AuthService {
  constructor(
    @InjectRepository(User)
    private readonly userRepo: Repository<User>,
    private readonly jwtService: JwtService,
  ) {}

  async sendSms(dto: SendSmsDto) {
    const code = process.env.SMS_PROVIDER === 'stub' ? '1234' : this.generateCode();
    const expires = new Date(Date.now() + 5 * 60 * 1000); // 5 минут

    let user = await this.userRepo.findOne({ where: { phone: dto.phone } });
    if (!user) {
      user = this.userRepo.create({ phone: dto.phone, cityId: 1 });
    }
    user.smsCode = code;
    user.smsCodeExpiresAt = expires;
    await this.userRepo.save(user);

    // TODO: отправить SMS через провайдера
    console.log(`[SMS] ${dto.phone} → код: ${code}`);
    return { success: true, message: 'Код отправлен' };
  }

  async registerV2(dto: RegisterV2Dto) {
    const user = await this.userRepo.findOne({ where: { phone: dto.phone } });
    if (!user) throw new BadRequestException('Пользователь не найден. Запросите код заново.');

    if (user.smsCode !== dto.code) throw new BadRequestException('Неверный код');
    if (!user.smsCodeExpiresAt || user.smsCodeExpiresAt < new Date()) throw new BadRequestException('Код истёк');

    if (dto.name) user.name = dto.name;
    if (dto.cityId) user.cityId = dto.cityId;
    user.smsCode = null as unknown as string;
    user.smsCodeExpiresAt = null as unknown as Date;

    const token = this.jwtService.sign({ sub: user.id });
    user.authToken = token;
    await this.userRepo.save(user);

    return { token, userId: user.id, name: user.name, phone: user.phone };
  }

  async registerWithToken(dto: RegisterWithTokenDto) {
    try {
      const payload = this.jwtService.verify<{ sub: string }>(dto.token);
      const user = await this.userRepo.findOne({ where: { id: payload.sub, phone: dto.phone } });
      if (!user) throw new UnauthorizedException();
      return { token: dto.token, userId: user.id, name: user.name, phone: user.phone };
    } catch {
      throw new UnauthorizedException('Недействительный токен');
    }
  }

  private generateCode(): string {
    return String(Math.floor(1000 + Math.random() * 9000));
  }
}
