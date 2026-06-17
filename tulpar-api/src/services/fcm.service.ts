import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { initializeApp, cert, getApps } from 'firebase-admin/app';
import { getMessaging } from 'firebase-admin/messaging';

@Injectable()
export class FcmService implements OnModuleInit {
  private readonly logger = new Logger(FcmService.name);
  private initialized = false;

  onModuleInit() {
    const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT;
    if (!serviceAccountJson) {
      this.logger.warn('FIREBASE_SERVICE_ACCOUNT не задан — push-уведомления отключены');
      return;
    }
    try {
      const serviceAccount = JSON.parse(serviceAccountJson);
      if (!getApps().length) {
        initializeApp({ credential: cert(serviceAccount) });
      }
      this.initialized = true;
      this.logger.log('Firebase Admin SDK инициализирован');
    } catch (e) {
      this.logger.error('Ошибка инициализации Firebase Admin SDK:', e.message);
    }
  }

  async sendToToken(token: string, title: string, body: string, data?: Record<string, string>) {
    if (!this.initialized || !token) return;
    try {
      await getMessaging().send({
        token,
        notification: { title, body },
        data,
        android: { priority: 'high', notification: { sound: 'default' } },
      });
    } catch (e) {
      this.logger.warn(`FCM send failed: ${e.message}`);
    }
  }
}
