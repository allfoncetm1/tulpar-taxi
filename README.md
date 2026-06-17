# Tulpar TAXI — Кандыагаш

Собственная платформа заказа такси для города Кандыагаш.

## Структура проекта

```
Tulpar Taxi/
├── TZ.md              # Техническое задание
├── logo/              # Брендинг (логотипы)
└── tulpar-android/    # Android-приложение (Kotlin + Compose)
```

## Android-приложение

- **Package:** `kz.tulpartaxi.kandyagash`
- **minSdk:** 24 | **targetSdk:** 34
- **Стек:** Kotlin, Jetpack Compose, Hilt, Retrofit, Room

### Запуск

1. Откройте папку `tulpar-android` в **Android Studio**
2. Дождитесь синхронизации Gradle
3. Запустите на эмуляторе или устройстве (Run ▶)

Или из командной строки:

```bash
cd tulpar-android
.\gradlew.bat assembleDebug
```

### Текущий этап (1)

- [x] Каркас проекта и Gradle
- [x] Брендинг Tulpar TAXI (цвета, логотип, splash)
- [x] Главный экран заказа (`StartActivity`)
- [x] Заглушки: Профиль, История, Справка
- [ ] Backend API (`tulpar-api`) — **следующий этап**
- [ ] Web-карта (`tulpar-map`, **GraphHopper Maps**)
- [ ] Режим водителя

## Документация

Полное ТЗ: [TZ.md](TZ.md)
