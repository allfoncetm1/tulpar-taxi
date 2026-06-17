import { Body, Controller, Post } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { AuthService } from './auth.service';
import { RegisterV2Dto, RegisterWithTokenDto, SendSmsDto } from './dto/register.dto';

@ApiTags('account')
@Controller('api/account')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('SendSms')
  @ApiOperation({ summary: 'Отправить SMS-код на номер телефона' })
  sendSms(@Body() dto: SendSmsDto) {
    return this.authService.sendSms(dto);
  }

  @Post('RegisterV2')
  @ApiOperation({ summary: 'Регистрация/вход по SMS-коду' })
  registerV2(@Body() dto: RegisterV2Dto) {
    return this.authService.registerV2(dto);
  }

  @Post('RegisterWithToken')
  @ApiOperation({ summary: 'Авторизация по сохранённому токену' })
  registerWithToken(@Body() dto: RegisterWithTokenDto) {
    return this.authService.registerWithToken(dto);
  }
}
