# Техническое задание (ТЗ)
## Мобильное приложение «Tulpar TAXI» — Кандыагаш

**Версия документа:** 1.1  
**Дата:** 06.06.2026  
**Изменение v1.1:** карты, routing и geocoding — **GraphHopper** (см. §4.4)  
**Продукт:** Tulpar TAXI (собственная платформа)  
**Референс-архитектура:** анализ типового taxi APK (структура client/driver, карты, REST API)  
**Целевой город (MVP):** Кандыагаш, Актюбинская область, Казахстан  
**Package ID:** `kz.tulpartaxi.kandyagash`

---

## 1. Цель проекта

Разработать **собственную** экосистему заказа такси **Tulpar TAXI** для города **Кандыагаш** с нуля:

- **Android-приложение** (клиент + водитель в одном APK или два отдельных);
- **Backend API** на домене `tulpartaxi.kz`;
- **Веб-карты** на `map.tulpartaxi.kz`;
- **Админ-панель** для диспетчера (опционально на этапе 2).

Функционал по структуре аналогичен современному taxi-приложению:

| Роль | Возможности |
|------|-------------|
| **Клиент** | Заказ на карте, расчёт цены, отслеживание водителя, история, оплата |
| **Водитель** | Приём заказов, GPS в фоне, статусы поездки, баланс, автопарк |
| **Диспетчер** | Мониторинг заказов, тарифы, водители (админка) |

> Tulpar TAXI — **независимый продукт**. Не использует чужие серверы, API и брендинг.

---

## 2. Общая архитектура Tulpar TAXI

```
┌──────────────────────────────────────────────────────────────────┐
│                     TULPAR TAXI ECOSYSTEM                         │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────────────────┐ │
│  │ Android App │   │  Admin Web  │   │   Map Web (WebView)     │ │
│  │  (Kotlin)   │   │  (React/Vue)│   │  map.tulpartaxi.kz      │ │
│  └──────┬──────┘   └──────┬──────┘   └───────────┬─────────────┘ │
│         │                 │                        │               │
│         └─────────────────┼────────────────────────┘               │
│                           ▼                                        │
│              ┌────────────────────────────┐                        │
│              │   API Gateway              │                        │
│              │   api.tulpartaxi.kz        │                        │
│              └─────────────┬──────────────┘                        │
│         ┌──────────────────┼──────────────────┐                    │
│         ▼                  ▼                  ▼                    │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────────┐         │
│  │ PostgreSQL  │   │    Redis    │   │ Firebase FCM    │         │
│  │ (основная   │   │ (кэш,       │   │ (push)          │         │
│  │  БД)        │   │  сессии)    │   │                 │         │
│  └─────────────┘   └─────────────┘   └─────────────────┘         │
│         ▲                  ▲                                       │
│  ┌──────┴──────┐   ┌──────┴──────┐   ┌─────────────────┐         │
│  │ geocoder.   │   │ routing.    │   │  GraphHopper    │         │
│  │ tulpartaxi  │   │ tulpartaxi  │──►│  Maps/Routing/  │         │
│  │ .kz (proxy) │   │ .kz (proxy) │   │  Geocoding API  │         │
│  └─────────────┘   └─────────────┘   └─────────────────┘         │
└──────────────────────────────────────────────────────────────────┘
```

### 2.1. Домены Tulpar TAXI

| Домен | Назначение |
|-------|------------|
| `https://tulpartaxi.kz` | Лендинг, политика конфиденциальности |
| `https://api.tulpartaxi.kz` | REST API (основной backend) |
| `https://map.tulpartaxi.kz/citymap` | Интерактивная карта города (WebView) |
| `https://map.tulpartaxi.kz/order` | Карта активного заказа |
| `https://map.tulpartaxi.kz/orgs` | Карта таксопарков / стоянок |
| `https://geocoder.tulpartaxi.kz` | Reverse geocoding (прокси → **GraphHopper Geocoding API**) |
| `https://routing.tulpartaxi.kz` | Маршруты и расстояние (прокси → **GraphHopper Routing API**) |
| `https://admin.tulpartaxi.kz` | Панель диспетчера (этап 2) |

### 2.2. Стек технологий

#### Mobile (Android)
| Компонент | Технология |
|-----------|------------|
| Язык | Kotlin |
| minSdk / targetSdk | 24 / 34 |
| UI | Jetpack Compose **или** XML + Material Design 3 |
| Архитектура | MVVM + Clean Architecture |
| DI | Hilt |
| Сеть | Retrofit 2 + OkHttp + Moshi |
| Локальная БД | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| GPS | Google Play Services Location |
| Push | Firebase Cloud Messaging |
| Карта | WebView + **GraphHopper Maps** (`map.tulpartaxi.kz`) |
| Auth token | EncryptedSharedPreferences |

#### Backend
| Компонент | Технология |
|-----------|------------|
| Framework | **NestJS (Node.js)** или **FastAPI (Python)** |
| БД | PostgreSQL 15+ |
| Кэш / очереди | Redis |
| Real-time | WebSocket + FCM |
| Карты / Routing / Geocoding | **GraphHopper** (Maps, Routing, Geocoding API) |
| GraphHopper API key | `GRAPHHOPPER_API_KEY` (env); dev-копия в `MAPS.TXT` (не в git) |
| Файлы | S3-compatible storage (логотипы, документы водителей) |
| Deploy | Docker + VPS (KZ, ближний регион) |

#### DevOps
- CI/CD: GitHub Actions / GitLab CI
- Мониторинг: Sentry + Firebase Crashlytics
- SSL: Let's Encrypt, **строгая TLS-валидация** (без `UnsafeOkHttpClient`)

---

## 3. Структура Android-приложения (модули)

Повторяет проверенную структуру taxi-приложения, адаптированную под Tulpar:

```
kz.tulpartaxi.kandyagash/
├── app/                          # Application, DI, Firebase
├── ui/
│   ├── start/                    # StartActivity — главный экран заказа
│   ├── map/                      # MapOrderFragment — карта + заказ
│   ├── profile/                  # ChangeProfileActivity
│   ├── history/                  # История поездок
│   ├── evaluation/               # Оценка поездки
│   ├── driver/                   # Режим водителя
│   └── info/                     # Справка, поддержка
├── data/
│   ├── api/                      # TulparApi, RoutingApi, GeocoderApi
│   ├── local/                    # Room DB (LocalDbHelper)
│   ├── repository/
│   └── models/
├── services/
│   ├── LocationForegroundService # GPS в фоне (водитель)
│   └── TulparFirebaseMessagingService
└── utils/
    ├── StaticConfig              # baseUrl, cityId, keys
    ├── LocaleHelper              # RU / KZ / EN
    └── PermissionManager
```

### 3.1. Экраны (Activities)

| Экран | Класс (Tulpar) | Назначение |
|-------|----------------|------------|
| Главный | `StartActivity` | LAUNCHER — заказ такси, карта |
| Профиль | `ProfileActivity` | Имя, телефон, язык |
| История | `HistoryActivity` | Список поездок |
| Оценка | `RatingActivity` | Оценка водителя после поездки |
| Успех | `OrderSuccessActivity` | Подтверждение заказа |
| Справка | `InfoActivity` | FAQ, WhatsApp поддержка |

> **SelectCityActivity** — **не нужен** в MVP (один город). `cityId = 1` зашит в `StaticConfig`.

### 3.2. Фрагменты (ключевые)

| Фрагмент | Назначение |
|----------|------------|
| `OrderMapFragment` | Основной экран: WebView + GraphHopper Maps + форма заказа |
| `AddressSearchFragment` | Поиск адреса по локальной БД |
| `DriverOrdersFragment` | Список входящих заказов (водитель) |
| `DriverActiveOrderFragment` | Активный заказ водителя |

### 3.3. Сервисы

| Сервис | Назначение |
|--------|------------|
| `LocationForegroundService` | Отправка GPS каждые 5–10 сек (водитель) |
| `TulparFirebaseMessagingService` | Push: новый заказ, статус, отмена |

---

## 4. Карты и геоданные Tulpar TAXI

### 4.1. Где хранятся карты

**Карты НЕ хранятся в APK.** Хранение — на сервере Tulpar:

| Уровень | Расположение | Содержимое |
|---------|--------------|------------|
| **Интерактивная карта** | `map.tulpartaxi.kz/citymap?userId=&cityId=1` | UI карты, маркеры, зоны |
| **Карта заказа** | `map.tulpartaxi.kz/order?orderId=` | Водитель + маршрут в реальном времени |
| **Организации** | `map.tulpartaxi.kz/orgs` | Стоянки, офисы |
| **Тайлы / UI карты** | GraphHopper Maps API | Подложка карты в `tulpar-map` |
| **Геокодинг** | `geocoder.tulpartaxi.kz/reverse` → GraphHopper | Адрес по lat/lng |
| **Маршруты** | `routing.tulpartaxi.kz/route` → GraphHopper | Расстояние, время, polyline |
| **Метаданные города** | PostgreSQL + seed `assets/tulpar.db` | Id, название, Viewbox, центр |
| **Адреса/POI** | PostgreSQL → sync → Room `Addresses` | Точки на карте города |

### 4.2. Локальная SQLite (Room) — seed `assets/tulpar.db`

#### Таблица `Cities`

```sql
CREATE TABLE Cities (
  Id          INTEGER PRIMARY KEY,
  NameKz      TEXT NOT NULL,
  NameRu      TEXT NOT NULL,
  NameEn      TEXT NOT NULL,
  RegionRu    TEXT,
  Lat         TEXT NOT NULL,
  Lng         TEXT NOT NULL,
  Viewbox     TEXT,       -- "minLat,minLng,maxLat,maxLng"
  Lineup      INTEGER,
  Version     INTEGER DEFAULT 1
);
```

**Seed — Кандыагаш (cityId = 1):**

```sql
INSERT INTO Cities (Id, NameKz, NameRu, NameEn, RegionRu, Lat, Lng, Viewbox, Lineup)
VALUES (
  1,
  'Қандыағаш',
  'Кандыагаш',
  'Kandyagash',
  'Актюбинская область',
  '49.465040',
  '57.410118',
  '49.42,57.35,49.51,57.47',
  1
);
```

#### Таблица `Addresses` (синхронизация с API)

```sql
CREATE TABLE Addresses (
  Id              TEXT PRIMARY KEY,
  NameKz          TEXT NOT NULL,
  NameRu          TEXT NOT NULL,
  NameEn          TEXT NOT NULL,
  CityId          INTEGER,
  CitySectionId   INTEGER,
  Lat             TEXT NOT NULL,
  Lng             TEXT NOT NULL,
  Version         INTEGER,
  Tags            TEXT,
  Radius          INTEGER,
  LastChange      TEXT NOT NULL,
  IsDelete        INTEGER DEFAULT 0
);
```

#### Таблица `Orders` (локальный кэш)

```sql
CREATE TABLE Orders (
  Id          TEXT PRIMARY KEY,
  CityId      INTEGER,
  FromStreet  TEXT,
  FromLat     TEXT,
  FromLng     TEXT,
  ToStreet    TEXT,
  ToLat       TEXT,
  ToLng       TEXT,
  Price       REAL,
  Status      TEXT,
  Date        TEXT
);
```

### 4.3. Секции города (районы Кандыагаша)

На backend таблица `city_sections`:

| Поле | Описание |
|------|----------|
| id | ID секции |
| city_id | 1 (Кандыагаш) |
| name_ru / name_kz | Название района |
| polygon | GeoJSON полигон зоны |
| min_price | Минимальная цена в зоне |

Примеры секций для настройки: центр, микрорайоны, промзона, вокзал.

### 4.4. GraphHopper — провайдер карт и маршрутизации

> **Решение MVP:** все карты, маршруты и геокодинг — через **GraphHopper**.  
> Self-hosted OSRM / Nominatim / Yandex MapKit / Leaflet+OSM — **не используются**.

| Сервис GraphHopper | Назначение в Tulpar | Прокси Tulpar |
|--------------------|---------------------|---------------|
| [Maps API](https://docs.graphhopper.com/openapi/maps) | Подложка карты, маркеры, маршрут на экране | `map.tulpartaxi.kz` (tulpar-map) |
| [Routing API](https://docs.graphhopper.com/openapi/routing) | `Estimate`, расстояние, ETA, polyline | `routing.tulpartaxi.kz` |
| [Geocoding API](https://docs.graphhopper.com/openapi/geocoding) | Reverse/forward geocode, поиск адреса | `geocoder.tulpartaxi.kz` |

**Документация:** https://docs.graphhopper.com/openapi  
**Аккаунт:** https://graphhopper.com/

**API-ключ:**
- Переменная окружения: `GRAPHHOPPER_API_KEY`
- Локальная dev-копия: файл `MAPS.TXT` в корне репозитория (**не коммитить в git**)
- Ключ передаётся только с backend/proxy — **не в Android APK**

**Поток данных:**

```
Android WebView (map.tulpartaxi.kz)
        │
        ▼
  tulpar-map (GraphHopper Maps JS)
        │
        ├── routing.tulpartaxi.kz ──► GraphHopper Routing API
        └── geocoder.tulpartaxi.kz ──► GraphHopper Geocoding API
```

**Viewbox Кандыагаша** (`49.42,57.35,49.51,57.47`) ограничивает область карты и валидацию точек заказа.

---

## 5. Backend Tulpar TAXI — база данных

### 5.1. ER-структура (PostgreSQL)

```
users ──────────────┬── orders ──── order_status_log
  │                 │
  ├── drivers ──────┘
  ├── cars
  └── firebase_tokens

cities ─── city_sections ─── addresses
  │
  └── tariffs ─── tariff_zones

payments
organizations
intercity_routes  (опционально, этап 3)
```

### 5.2. Основные таблицы

**`users`**
```sql
id UUID PRIMARY KEY,
phone VARCHAR(20) UNIQUE NOT NULL,
name VARCHAR(100),
is_driver BOOLEAN DEFAULT FALSE,
city_id INTEGER REFERENCES cities(id),
auth_token VARCHAR(512),
firebase_token TEXT,
created_at TIMESTAMPTZ DEFAULT NOW()
```

**`cities`**
```sql
id SERIAL PRIMARY KEY,
name_kz VARCHAR(100), name_ru VARCHAR(100), name_en VARCHAR(100),
lat DECIMAL(10,7), lng DECIMAL(10,7),
viewbox VARCHAR(100),
region_ru VARCHAR(100),
is_active BOOLEAN DEFAULT TRUE,
version INTEGER DEFAULT 1
```

**`addresses`**
```sql
id UUID PRIMARY KEY,
city_id INTEGER REFERENCES cities(id),
city_section_id INTEGER,
name_kz TEXT, name_ru TEXT, name_en TEXT,
lat DECIMAL(10,7), lng DECIMAL(10,7),
tags TEXT, radius INTEGER,
version INTEGER, is_deleted BOOLEAN DEFAULT FALSE,
updated_at TIMESTAMPTZ
```

**`orders`**
```sql
id UUID PRIMARY KEY,
client_id UUID REFERENCES users(id),
driver_id UUID REFERENCES users(id),
city_id INTEGER,
from_lat DECIMAL(10,7), from_lng DECIMAL(10,7), from_address TEXT,
to_lat DECIMAL(10,7), to_lng DECIMAL(10,7), to_address TEXT,
estimated_price DECIMAL(10,2), final_price DECIMAL(10,2),
status VARCHAR(30),  -- new, offered, accepted, arrived, in_progress, completed, cancelled
comment TEXT,
created_at TIMESTAMPTZ, completed_at TIMESTAMPTZ
```

**`drivers`**
```sql
user_id UUID PRIMARY KEY REFERENCES users(id),
car_model VARCHAR(100), car_number VARCHAR(20), car_color VARCHAR(50),
balance DECIMAL(10,2) DEFAULT 0,
status VARCHAR(20),  -- offline, online, busy
last_lat DECIMAL(10,7), last_lng DECIMAL(10,7),
last_location_at TIMESTAMPTZ
```

**`tariffs`**
```sql
id SERIAL PRIMARY KEY,
city_id INTEGER,
name VARCHAR(100),
base_price DECIMAL(10,2),
price_per_km DECIMAL(10,2),
min_price DECIMAL(10,2),
is_active BOOLEAN DEFAULT TRUE
```

---

## 6. REST API Tulpar TAXI

**Base URL:** `https://api.tulpartaxi.kz`  
**Формат:** JSON, POST (совместимость со структурой taxi REST)  
**Auth:** Header `Authorization: Bearer {token}`

### 6.1. Account — `/api/account/`

| Endpoint | Описание | Тело запроса |
|----------|----------|--------------|
| `POST /RegisterV2` | Регистрация по SMS | `{ phone, code, name, cityId }` |
| `POST /RegisterWithToken` | Авторизация по token | `{ phone, token }` |
| `POST /RegUser` | Первичная регистрация | `{ phone, name }` |

### 6.2. Client — `/api/client/`

| Endpoint | Описание |
|----------|----------|
| `POST /GetStartData` | Стартовые данные: user, city, tariffs, flags |
| `POST /GetCity` | `{ cityId: 1 }` → Viewbox, тарифы, настройки |
| `POST /LoadCities` | Список городов (MVP: только Кандыагаш) |
| `POST /LoadAddresses` | `{ cityId, version }` → delta адресов |
| `POST /UpdateLocation` | `{ lat, lng, isDriver }` — GPS |
| `POST /GetOrderHistory` | История заказов клиента |
| `POST /GetCars` | Автомобили водителя |
| `POST /AddCar` | Добавить авто |
| `POST /DeleteCar` | Удалить авто |
| `POST /SelectCar` | Выбрать активное авто |
| `POST /ReverseScr` | Reverse geocode через backend |
| `POST /CheckAppStatus` | `{ os: "android", version }` — проверка обновлений |

### 6.3. Services (заказы) — `/api/services/`

| Endpoint | Описание |
|----------|----------|
| `POST /Estimate` | Расчёт цены `{ fromLat, fromLng, toLat, toLng, cityId }` |
| `POST /createNewCustom` | Создать заказ |
| `POST /getNewCustoms` | Активные заказы (водитель) |
| `POST /AcceptOffer` | Принять предложение |
| `POST /RejectOffer` | Отклонить |
| `POST /NewOffer` | Новое предложение цены |
| `POST /sendOffer` | Отправить оффер |
| `POST /getOffer` | Получить оффер |
| `POST /getOfferStatus` | Статус оффера |
| `POST /cancelOffer` | Отмена оффера |
| `POST /AcceptOrder` | Принять заказ (водитель) |
| `POST /Arrived` | Водитель прибыл |
| `POST /arrivedDriver` | Подтверждение прибытия |
| `POST /SetAction` | Смена статуса поездки |
| `POST /ChangePrice` | Изменить цену |
| `POST /GetBalance` | Баланс водителя |
| `POST /getMyPayments` | История платежей |
| `POST /payment` | Оплата |
| `POST /getHistoryByClient` | История клиента |
| `POST /getHistoryByDriver` | История водителя |
| `POST /getDriver` | Данные водителя |
| `POST /getDriverInfo` | Профиль водителя |
| `POST /saveDriverInfo` | Сохранить профиль |
| `POST /getUserId` | ID пользователя |
| `POST /checkToken` | Проверка токена |
| `POST /UpdateDeviceId` | FCM token |
| `POST /changeMyName` | Смена имени |
| `POST /getCities` | Список городов |
| `POST /FindCities` | Поиск города |
| `POST /getOrgs` | Организации на карте |
| `POST /getCategories` | Категории тарифов |
| `POST /EnableTariff` | Включить тариф |
| `POST /GetCustoms` | Список заказов |
| `POST /getAcceptedCustom` | Принятые заказы |
| `POST /acceptNewCustom` | Принять новый заказ |
| `POST /cancelNewCustom` | Отменить заказ |
| `POST /checkPoint` | Проверка точки в зоне города |
| `POST /checkTicket` | Проверка промокода |

### 6.4. Intercity (этап 3, опционально)

- `createIntercity`, `deleteIntercity`, `getIntercity`, `GetIntercityModel`
- `enableIntercityPush`, `disableIntercityPush`

### 6.5. Truck / груз (этап 3, опционально)

- `createTruck`, `deleteTruck`, `getTruck`, `enableTruckPush`, `getTruckPushStatus`

---

## 7. Сценарии пользователя

### 7.1. Клиент — заказ поездки

```
1. Запуск → GetStartData (cityId=1, Kandyagash)
2. LoadAddresses → кэш POI в Room
3. OrderMapFragment → WebView map.tulpartaxi.kz/citymap
4. Выбор «Откуда» / «Куда» (тап или поиск)
5. Estimate → показ цены
6. createNewCustom → заказ создан
7. Push водителям (FCM)
8. Водитель AcceptOrder → клиент видит map.tulpartaxi.kz/order
9. Статусы: arrived → in_progress → completed
10. RatingActivity → оценка
```

### 7.2. Водитель — рабочая смена

```
1. Авторизация → isDriver=true
2. SelectCar → выбор авто
3. Включить «На линии» → LocationForegroundService
4. UpdateLocation каждые 5–10 сек
5. Push «Новый заказ» → getNewCustoms
6. AcceptOrder / RejectOffer
7. SetAction: arrived → start → complete
8. GetBalance → просмотр заработка
```

---

## 8. Функциональные требования Tulpar TAXI

### 8.1. Общие (FR)

| ID | Требование |
|----|------------|
| FR-01 | Android 7.0+ (API 24), target API 34 |
| FR-02 | Языки: русский, казахский, английский |
| FR-03 | Город по умолчанию: **Кандыагаш (cityId=1)**, без выбора города |
| FR-04 | Центр карты: `49.465040, 57.410118` |
| FR-05 | Offline-кэш адресов и города в Room |
| FR-06 | Push через Firebase Cloud Messaging |
| FR-07 | Фоновый GPS для водителей (Foreground Service) |
| FR-08 | Брендинг: логотип, цвета, название **Tulpar TAXI** |
| FR-09 | Поддержка WhatsApp для связи с диспетчером |
| FR-10 | Звонок водителю / клиенту (CALL_PHONE) |

### 8.2. Карта (MAP)

| ID | Требование |
|----|------------|
| MAP-01 | Карта через WebView (`map.tulpartaxi.kz`) на **GraphHopper Maps** |
| MAP-02 | Viewbox Кандыагаша с сервера — ограничение области |
| MAP-03 | Sync адресов: `LoadAddresses` → Room |
| MAP-04 | Geocoder: `geocoder.tulpartaxi.kz` → GraphHopper Geocoding API |
| MAP-05 | Routing: `routing.tulpartaxi.kz` → GraphHopper Routing API |
| MAP-10 | API-ключ GraphHopper только на сервере (`GRAPHHOPPER_API_KEY`) |
| MAP-06 | Поиск адресов локально (NameRu/NameKz/NameEn) |
| MAP-07 | CitySectionId — районы города |
| MAP-08 | Live-карта заказа с позицией водителя |
| MAP-09 | JavaScript bridge WebView ↔ Android (выбор точки) |

### 8.3. Backend (BE)

| ID | Требование |
|----|------------|
| BE-01 | REST API на `api.tulpartaxi.kz` |
| BE-02 | PostgreSQL — все бизнес-данные |
| BE-03 | Redis — сессии, online-водители |
| BE-04 | FCM — push при новом заказе и смене статуса |
| BE-05 | Admin panel (этап 2) — тарифы, водители, заказы |
| BE-06 | Rate limiting и JWT auth |
| BE-07 | HTTPS only, без self-signed в production |
| BE-08 | Логирование заказов и GPS |

---

## 9. Брендинг Tulpar TAXI

| Элемент | Значение |
|---------|----------|
| Название приложения | **Tulpar TAXI** |
| Package ID | `kz.tulpartaxi.kandyagash` |
| Основной цвет | `#E85D04` (оранжевый) / `#1A1A2E` (тёмный) — уточнить в макетах |
| Иконка | Силуэт тулпара (скакун) + такси |
| Splash screen | Логотип Tulpar TAXI + «Кандыагаш» |
| Firebase project | `tulpar-taxi-kz` |
| Google Play | «Tulpar TAXI — Кандыагаш» |

---

## 10. Разрешения Android

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
```

---

## 11. Безопасность

| Правило | Реализация |
|---------|------------|
| TLS | Строгая проверка сертификатов (OkHttp CertificatePinner) |
| Токены | EncryptedSharedPreferences / Keystore |
| API | JWT + refresh token, срок жизни 7–30 дней |
| SMS | Rate limit на RegisterV2 |
| GPS | Только при активной смене водителя |
| PII | Шифрование phone в логах |
| Admin | 2FA для admin.tulpartaxi.kz |

---

## 12. Этапы разработки

| # | Этап | Срок | Deliverables |
|---|------|------|--------------|
| 1 | Проектирование | 1–2 нед | API spec, ER-диаграмма, UI-макеты Figma |
| 2 | Backend MVP | 3–4 нед | Auth, cities, addresses, Estimate, orders, FCM |
| 3 | Map-сервис | 2–3 нед | tulpar-map + GraphHopper Maps; прокси routing/geocoder |
| 4 | Android клиент | 4–6 нед | Заказ, карта, профиль, история |
| 5 | Android водитель | 3–4 нед | Приём заказов, GPS, баланс |
| 6 | QA + нагрузка | 2 нед | Тест-план, баги, оптимизация |
| 7 | Публикация | 1 нед | Google Play, tulpartaxi.kz |

**MVP (этапы 1–5):** ~14–18 недель, команда 2–3 разработчика.  
**Полный продукт (+ admin, intercity, truck):** +6–8 недель.

---

## 13. Критерии приёмки MVP

- [ ] Приложение **Tulpar TAXI** запускается с городом Кандыагаш без выбора города
- [ ] Карта центрируется на 49.465°N, 57.410°E
- [ ] Клиент выбирает «Откуда» и «Куда» на карте
- [ ] Цена рассчитывается до подтверждения (`Estimate`)
- [ ] Push приходит водителю при новом заказе
- [ ] Водитель принимает заказ — клиент видит его на карте
- [ ] GPS водителя обновляется в фоне (≤ 10 сек)
- [ ] История поездок отображается
- [ ] Работает offline-просмотр кэшированных адресов
- [ ] Backend на `api.tulpartaxi.kz` — не зависит от сторонних taxi-сервисов
- [ ] Опубликовано в Google Play под брендом **Tulpar TAXI**

---

## 14. Структура репозиториев

```
tulpar-taxi/
├── tulpar-android/          # Kotlin Android app
├── tulpar-api/                # NestJS / FastAPI backend
├── tulpar-map/                # Web-карты (GraphHopper Maps JS)
├── tulpar-admin/              # Admin panel (этап 2)
├── tulpar-infra/              # Docker, nginx, CI/CD
└── docs/
    ├── TZ_Tulpar_TAXI.md      # этот документ
    ├── api/openapi.yaml       # OpenAPI спецификация
    └── db/schema.sql          # PostgreSQL schema
```

---

## 15. Отличия Tulpar TAXI от референс-APK

| Компонент | Референс (типовой taxi APK) | Tulpar TAXI |
|-----------|----------------------------|-------------|
| Бренд | Чужой | **Tulpar TAXI** |
| Backend | Чужие домены | **api.tulpartaxi.kz** |
| Карты | Чужие URL | **map.tulpartaxi.kz** + **GraphHopper** |
| Routing / Geocoding | OSRM / Nominatim | **GraphHopper API** (через прокси Tulpar) |
| Package | `kz.internet_taxi_*` | **`kz.tulpartaxi.kandyagash`** |
| Seed DB | `derekqor.db` | **`tulpar.db`** |
| Города | Мультигород | **Kandyagash only (MVP)** |
| API класс | `taxiAPI` | **`TulparApi`** |
| Config | `StaticValues` | **`StaticConfig`** |
| FCM Service | `MyFirebaseMessagingService` | **`TulparFirebaseMessagingService`** |
| Зависимость | Сторонний сервер | **Полностью свой** |

---

## 16. Резюме: карты Tulpar TAXI

```
┌─────────────────────────────────────────────────────────────────┐
│  КАРТЫ TULPAR TAXI — GraphHopper + СВОИ ДОМЕНЫ                   │
├─────────────────────────────────────────────────────────────────┤
│  Провайдер: GraphHopper (Maps + Routing + Geocoding)             │
│  Документация: https://docs.graphhopper.com/openapi              │
│  Ключ: GRAPHHOPPER_API_KEY (сервер), dev → MAPS.TXT              │
├─────────────────────────────────────────────────────────────────┤
│  1. map.tulpartaxi.kz/citymap      → GraphHopper Maps (WebView)  │
│  2. map.tulpartaxi.kz/order        → карта заказа + маршрут GH   │
│  3. map.tulpartaxi.kz/orgs         → таксопарки                 │
│  4. geocoder.tulpartaxi.kz         → GraphHopper Geocoding       │
│  5. routing.tulpartaxi.kz          → GraphHopper Routing         │
│  6. api.tulpartaxi.kz              → Viewbox, POI, заказы       │
│  7. assets/tulpar.db (APK)         → seed города (метаданные)    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 17. Контакты и поддержка (шаблон)

| Канал | Значение |
|-------|----------|
| Сайт | https://tulpartaxi.kz |
| WhatsApp | +7 (XXX) XXX-XX-XX |
| Email | support@tulpartaxi.kz |
| Telegram | @TulparTaxiKandyagash |

---

*Документ описывает разработку **собственного** продукта Tulpar TAXI. Архитектура основана на анализе типового taxi APK; все домены, брендинг и backend — независимые.*
