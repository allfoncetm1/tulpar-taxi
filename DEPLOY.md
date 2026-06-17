# Deploy guide — Tulpar Taxi на Render + tulpar.space

## Архитектура

```
tulpar.space (Hostinger)
├── api.tulpar.space  →  Render (NestJS backend)
└── map.tulpar.space  →  Render (Express map server)

Render PostgreSQL (Frankfurt)  →  users / orders / cities
```

В корне проекта лежит `render.yaml` — Blueprint, который описывает все сервисы. Render развернёт всё одной кнопкой.

---

## Шаг 1. GitHub репозиторий

Render деплоит из GitHub. Создай если нет:

```bash
cd "E:/Tulpar Taxi"
git init
git add .
git commit -m "Initial commit"
```

Создай пустой репо на github.com и привяжи:
```bash
git remote add origin https://github.com/YOURNAME/tulpar-taxi.git
git branch -M main
git push -u origin main
```

**Уже защищено `.gitignore`** — `.env`, `google-services.json`, `*.jks`, `*.keystore`, `node_modules`, `build/`, `dist/` не попадут в репо.

---

## Шаг 2. Render аккаунт

1. Зарегистрируйся на [render.com](https://render.com) через GitHub
2. **Authorize** доступ к репозиторию `tulpar-taxi`

---

## Шаг 3. Развернуть Blueprint

1. В Render Dashboard: **New → Blueprint**
2. Подключи репозиторий `tulpar-taxi`
3. Render найдёт `render.yaml` и покажет 3 сервиса:
   - `tulpar-db` (PostgreSQL, free)
   - `tulpar-api` (Web Service, free)
   - `tulpar-map` (Web Service, free)
4. Нажми **Apply**

Render создаст всё за 5-10 минут. Postgres стартует сразу, backend ждёт DB.

---

## Шаг 4. Задать секреты

После создания зайди в каждый сервис → **Environment** → добавь:

**В `tulpar-api`:**
- `FIREBASE_SERVICE_ACCOUNT` = весь JSON из `tulpar-taxi-firebase-adminsdk-*.json` одной строкой

**В `tulpar-map`:**
- `MAPBOX_TOKEN` = `pk.eyJ1IjoiYWxsZm9uY2V0bTEi...` (твой токен)

После сохранения — Render автоматически перезапустит сервисы.

---

## Шаг 5. Проверить что работает

После деплоя Render даст URL вида:
- `https://tulpar-api.onrender.com/api/docs` → Swagger
- `https://tulpar-map.onrender.com/config` → JSON с mapboxToken

Открой эти URL в браузере. Если работают — деплой успешен.

⚠️ **На free плане первый запрос после простоя медленный** (~30 сек) — сервис засыпает после 15 минут без трафика.

---

## Шаг 6. Подключить домен tulpar.space

### В Render для каждого сервиса:

**tulpar-api:**
1. Settings → **Custom Domains** → **Add Custom Domain**
2. Введи `api.tulpar.space`
3. Render покажет CNAME запись типа `tulpar-api.onrender.com`

**tulpar-map:**
1. Settings → **Custom Domains** → **Add Custom Domain**
2. Введи `map.tulpar.space`
3. Render покажет CNAME

### В Hostinger DNS Zone Editor (`tulpar.space`):

| Type  | Name | Points to                       | TTL  |
|-------|------|---------------------------------|------|
| CNAME | api  | `tulpar-api.onrender.com`       | 3600 |
| CNAME | map  | `tulpar-map.onrender.com`       | 3600 |

Через 5-30 минут Render выдаст SSL автоматически (Let's Encrypt).

Проверь:
- `https://api.tulpar.space/api/docs`
- `https://map.tulpar.space/config`

---

## Шаг 7. Release APK для Android

В `tulpar-android/app/build.gradle.kts` уже прописан правильный URL:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://api.tulpar.space\"")
buildConfigField("String", "MAP_BASE_URL", "\"https://map.tulpar.space\"")
```

### Сгенерировать keystore (один раз):
```bash
cd "E:/Tulpar Taxi/tulpar-android"
keytool -genkey -v -keystore tulpar-release.jks -alias tulpar -keyalg RSA -keysize 2048 -validity 10000
```
Запомни пароли! Без них не сможешь обновлять APK в будущем.

### Добавить подпись в `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../tulpar-release.jks")
            storePassword = "ТВОЙ_ПАРОЛЬ"
            keyAlias = "tulpar"
            keyPassword = "ТВОЙ_ПАРОЛЬ"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }
    }
}
```

### Собрать:
```bash
./gradlew assembleRelease
```

APK будет в `app/build/outputs/apk/release/app-release.apk`. Это файл для теста на телефоне.

---

## FAQ

**Render Free Tier лимиты:**
- Web service спит после 15 минут без трафика (первый запрос ~30 сек)
- 750 часов/мес на все сервисы вместе
- PostgreSQL free: 1 ГБ, истекает через 90 дней (нужно либо upgrade либо перенос)
- Bandwidth: 100 ГБ/мес

**Когда переходить на платный:**
- Если нужно чтоб не засыпало → Starter $7/мес
- Postgres надолго → $7/мес
- Итого production: $14/мес для backend + DB

**Деплой обновлений:**
- `git push` → Render автоматически пересоберёт
- Можно настроить deploy hooks для CI

**Логи и мониторинг:**
- Render Dashboard → сервис → **Logs** (real-time)
- Health check каждые 30 сек по `/api/docs` и `/config`

**SMS-провайдер:**
- Сейчас `SMS_PROVIDER=stub` — код пишется в логах Render
- Для прода подключить SMSC.kz или Mobizon — добавить `SMS_PROVIDER=smsc` и креды

**Google Play позже:**
- Developer аккаунт $25 один раз
- `./gradlew bundleRelease` → AAB файл
- Загрузить в Play Console → Internal Testing → потом Production
