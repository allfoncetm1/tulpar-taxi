# ТЗ v2 — Roadmap апгрейдов Tulpar Taxi

**Дата:** 2026-06-18
**Текущий статус:** Клиентский APK в проде (api.tulpar.space + map.tulpar.space), 14 фич готово, 23 API эндпоинта работают.
**Главный блокер:** нет водительского приложения → заказы зависают в статусе `new`.

---

## P1 — критично, делаем сейчас

### 1. Driver-приложение (отдельный APK)

**Зачем:** Сейчас никто не может принять заказ. Клиент отправляет → ничего не происходит. Без этого приложение не работает в реальности.

**Технические детали:**
- Новый Android-проект `tulpar-android-driver/` или buildVariant внутри текущего
- Пакет: `kz.tulpartaxi.driver.kandyagash`
- Отдельный keystore
- Использует тот же backend `api.tulpar.space`

**Экраны:**
- `AuthActivity` — телефон + SMS (как у клиента)
- `OnDutyActivity` — главный экран: переключатель «онлайн/оффлайн», карта с моей позицией
- `OrderListActivity` — список доступных заказов (`/getNewCustoms` каждые 5 сек polling)
- `OrderDetailActivity` — детали + кнопка «Принять» (`/AcceptOrder`)
- `ActiveOrderActivity` — навигация к клиенту, статусы (`arrived` / `start` / `complete`), кнопка отмены
- `BalanceActivity` — баланс водителя, история выплат
- `ProfileActivity` — данные авто, документы

**API уже готово:** `/getNewCustoms`, `/AcceptOrder`, `/SetAction`, `/UpdateLocation`, `/GetBalance`, `/getDriverInfo`, `/saveDriverInfo`

**Дополнительные API нужны:**
- `POST /api/services/SetOnlineStatus { online: boolean }` — переключатель онлайн/оффлайн
- `POST /api/services/GetActiveOrder` — текущий активный заказ водителя
- `GET /api/services/DriverOrderUpdates` (WebSocket или SSE) — push новых заказов

**Срок:** 3-5 дней
**Зависимости:** keystore (отдельный), Firebase проект (можно тот же)

---

### 2. Реальный SMS-провайдер (SMSC.kz или Mobizon)

**Зачем:** Сейчас код всегда `1234` (заглушка). Никто кроме разработчика не залогинится. Это блокер для теста с реальными пользователями.

**Технические детали:**

Backend `tulpar-api/src/auth/auth.service.ts`:
```ts
// TODO: отправить SMS через провайдера
```
Заменить на реальный вызов.

**Провайдер:** SMSC.kz (~5 ₸/SMS, стартер пакет 1000 SMS ≈ 5000 ₸)
- HTTP API: `https://smsc.kz/sys/send.php?login=X&psw=Y&phones=+7...&mes=Код:1234`
- Нужно завести юридическое лицо или ИП для договора

**Env vars:**
```
SMS_PROVIDER=smsc
SMSC_LOGIN=tulpar_taxi
SMSC_PASSWORD=...
SMSC_SENDER=TULPAR
```

**Реализация:**
```ts
// src/auth/sms.service.ts
@Injectable()
export class SmsService {
  async send(phone: string, code: string): Promise<void> {
    if (process.env.SMS_PROVIDER === 'stub') return;
    const url = `https://smsc.kz/sys/send.php`;
    await fetch(`${url}?login=${LOGIN}&psw=${PSW}&phones=${phone}&mes=Tulpar: код ${code}`);
  }
}
```

**Срок:** 3 часа + 1-2 дня на регистрацию у провайдера
**Бюджет:** ~5000 ₸/мес стартер

---

### 3. Отмена заказа клиентом + real-time статус

**Зачем:** Сейчас клиент создаёт заказ и видит «Ищем водителя...» — но не знает что происходит. Кнопки отмены нет. Через 30 минут он уйдёт из приложения.

**Технические детали:**

**Android:** новый экран `ActiveOrderScreen.kt`:
- Показывается когда `formState.activeOrderId != null`
- Отображает статус: «Поиск...» / «Водитель Алмат, +77011234567, KIA Rio 123ABC, едет 5 мин» / «Прибыл» / «В пути»
- Кнопка «Отменить заказ» → `/SetAction { orderId, action: 'cancel' }`
- Polling `/GetMyActiveOrder` каждые 5 сек (пока не сделан WebSocket)

**Backend:** новый эндпоинт
```ts
@Post('GetMyActiveOrder')
getMyActiveOrder(@Request() req) {
  return this.svc.getMyActiveOrder(req.user.id);
  // Возвращает: { orderId, status, driver?, eta?, fromAddress, toAddress }
}
```

**Срок:** 1 день

---

## P2 — следующая итерация

### 4. Карта водителя в реальном времени

**Зачем:** После принятия — показать машинку на карте, маршрут к клиенту, ETA.

**Технические детали:**
- На `ActiveOrderScreen` встроить Mapbox с маркером водителя
- Водитель шлёт `/UpdateLocation` каждые 3 сек (уже есть в API)
- Клиент polling `/GetDriverLocation { orderId }` каждые 3 сек
- Mapbox Directions API для маршрута: ~$0.50 за 1000 запросов

**Backend:**
```ts
@Post('GetDriverLocation')
getDriverLocation(@Body() { orderId }: { orderId: string }) {
  return this.svc.getDriverLocationByOrder(orderId);
}
```

**Срок:** 2 дня

---

### 5. Мульти-город

**Зачем:** Расширение в Хромтау, Шалкар, Байконур (10 городов как у SoonCar).

**Технические детали:**
- API `/LoadCities` уже есть
- Новый экран `SelectCityActivity` при первом запуске
- Динамический `viewbox` карты по выбранному городу
- Сохранение `cityId` в `TokenStorage`
- Hostname карты должен принимать `?cityId=` (уже принимает)

**Срок:** 1 день

---

### 6. WhatsApp поддержка + Congrats screen

**Зачем:** Из ТЗ конкурента — фичи которые повышают удержание.

**WhatsApp:**
```kotlin
// InfoActivity.kt
Button(onClick = {
    val intent = Intent(Intent.ACTION_VIEW,
        Uri.parse("https://wa.me/77001234567?text=Привет, нужна помощь по заказу"))
    startActivity(intent)
}) { Text("Написать в WhatsApp") }
```

**Congrats:**
- Показывается один раз после первой регистрации (`tokenStorage.isFirstLaunch`)
- Анимация конфетти + «Добро пожаловать в Tulpar Taxi!»

**Срок:** 4 часа

---

### 7. Foreground service для удержания push в фоне

**Зачем:** На Xiaomi/Realme push не приходят когда телефон в режиме экономии.

**Технические детали:**
- `LocationForegroundService` — крутится пока есть активный заказ
- Постоянное уведомление: «Ждём водителя» / «Водитель в пути»
- Запрос `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` при первом активном заказе

**Срок:** 1 день

---

### 8. Render upgrade на Starter план

**Зачем:**
- Free план: сервис засыпает после 15 мин, первый запрос 30 сек (клиент уйдёт)
- Postgres free истечёт через 90 дней — потеряем все данные

**Что нужно:**
- $7/мес × 2 (backend + map) = $14/мес
- $7/мес Postgres Starter = $7/мес
- Итого: ~$21/мес ≈ 12 000 ₸/мес

**Срок:** 5 минут на upgrade
**Когда делать:** до первого реального пользователя

---

## P3 — позже

### 9. Google Play публикация

**Что нужно:**
- Google Developer аккаунт ($25 один раз)
- AAB: `./gradlew bundleRelease`
- Иконка 512×512 (есть в `/logo`)
- Скриншоты: 4-8 шт телефон + 4 шт планшет
- Описание на казахском и русском
- Privacy Policy URL (хостим на tulpar.space)

**Этапы:**
1. Internal Testing (закрытое тестирование)
2. Open Testing (бета)
3. Production

**Срок:** 1 неделя на оформление + 1-3 дня ревью Google

---

### 10. Промокоды и реферальная система

**Что нужно:**
- Таблица `promo_codes` (code, discount, expires_at, used_by)
- Таблица `referrals` (inviter_id, invitee_id, reward)
- API: `/ApplyPromo`, `/GetReferralCode`
- UI: поле промокода в заказе, экран «Пригласить друга»

**Срок:** 3 дня

---

### 11. Безналичная оплата (Kaspi Pay / Halyk)

**Что нужно:**
- Договор с банком на эквайринг
- API Kaspi/Halyk через их SDK
- Сохранение карт пользователя

**Срок:** 1-2 недели + время на договор (1-2 месяца)

---

### 12. Аналитика и админ-панель

**Что нужно:**
- Веб-дашборд для диспетчера (React + та же база)
  - Все активные заказы по городам
  - Карта с водителями онлайн
  - Графики: выручка по дням, количество поездок
- Google Analytics в APK (события: регистрация, заказ создан, оценка)

**Срок:** 1 неделя

---

## Дополнительные технические задачи (не относятся к фичам)

### Backend cleanup
- [ ] Health endpoint `/api/health` (Render использует `/api/docs` сейчас — не оптимально)
- [ ] Логирование в Sentry (для отлова крашей в проде)
- [ ] Rate limiting на `/SendSms` (нельзя слать SMS каждую секунду)
- [ ] Перевод `synchronize: true` → миграции через `typeorm migration:generate`

### Android cleanup
- [ ] ProGuard правила для release сборки (сейчас `isMinifyEnabled = false`)
- [ ] Размер APK — сейчас 14 МБ, можно уменьшить до ~8 МБ через minify
- [ ] Crash reporting через Firebase Crashlytics

### Безопасность
- [ ] Перенести `keystore password` из `build.gradle.kts` в `~/.gradle/gradle.properties` (сейчас в коде, потенциально в git)
- [ ] Добавить `network_security_config.xml` чтобы прибил debug http://10.0.2.2 в release
- [ ] Подписать APK через v2/v3/v4 (сейчас только v1+v2)

---

## Бюджет на месяц production

| Сервис | Цена |
|--------|------|
| Render Web × 2 | $14 |
| Render Postgres Starter | $7 |
| Mapbox tiles (до 50k MAU бесплатно) | $0 |
| SMSC.kz (1000 SMS) | ~12 ₸ × 1000 = 12 000 ₸ |
| Hostinger домен | ~5000 ₸/год |
| **Итого** | **~24 000 ₸/мес** |

При 100 поездок/день — окупится при средней комиссии 100 ₸/поездка.

---

## Порядок работ (рекомендованный)

**Спринт 1 (1 неделя):**
- День 1-2: Driver-приложение (P1.1) — каркас + auth
- День 3-4: Driver-приложение — заказы и статусы
- День 5: SMS-провайдер (P1.2)
- День 6-7: Отмена + real-time статус (P1.3)

**Спринт 2 (1 неделя):**
- День 1-2: Карта водителя real-time (P2.4)
- День 3: Мульти-город (P2.5)
- День 4: WhatsApp + Congrats (P2.6)
- День 5: Foreground service (P2.7)
- День 6: Render upgrade + Sentry
- День 7: Полный тест на реальных телефонах

**Спринт 3 (1-2 недели):**
- Google Play оформление
- Промокоды
- Аналитика
- Подготовка к продвижению
