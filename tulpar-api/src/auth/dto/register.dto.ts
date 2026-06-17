import { ApiProperty } from '@nestjs/swagger';
import { IsBoolean, IsNotEmpty, IsNumberString, IsOptional, IsString, Length } from 'class-validator';

export class SendSmsDto {
  @ApiProperty({ example: '+77001234567' })
  @IsNotEmpty()
  @IsString()
  phone: string;
}

export class RegisterV2Dto {
  @ApiProperty({ example: '+77001234567' })
  @IsNotEmpty()
  @IsString()
  phone: string;

  @ApiProperty({ example: '1234' })
  @IsNotEmpty()
  @IsNumberString()
  @Length(4, 6)
  code: string;

  @ApiProperty({ example: 'Алибек', required: false })
  @IsOptional()
  @IsString()
  name?: string;

  @ApiProperty({ example: 1 })
  cityId: number;

  @ApiProperty({ example: false, required: false })
  @IsOptional()
  @IsBoolean()
  isDriver?: boolean;
}

export class RegisterWithTokenDto {
  @ApiProperty({ example: '+77001234567' })
  @IsNotEmpty()
  @IsString()
  phone: string;

  @ApiProperty()
  @IsNotEmpty()
  @IsString()
  token: string;
}
