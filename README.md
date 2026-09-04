# SpecInspect

[![Google Play](https://img.shields.io/badge/Google%20Play-com.alki.specinspect-brightgreen)](https://play.google.com/store/apps/details?id=com.alki.specinspect)

Мобильное приложение для ревью спецификаций в формате swipe-карточек: подключаете GitHub-репозиторий,
приложение разбирает `spec.md` файлы на требования и сценарии, а вы просматриваете их по одному —
свайп вправо, если требование корректное, свайп влево, если есть проблемы.

Приложение в Google Play: <https://play.google.com/store/apps/details?id=com.alki.specinspect>

Kotlin Multiplatform + Compose Multiplatform, общий код и UI для **Android** и **iOS**.

## Зачем это нужно

Спецификации, написанные для AI-агентов (OpenSpec, spec-kit), быстро растут в объёме, и вычитывать их
в редакторе неудобно. SpecInspect превращает папку со спецификациями в последовательный review flow:
одна карточка — один сценарий, с сохранением истории решений, статистикой и выгрузкой отчёта по
проблемным местам.

## Возможности

### Импорт из GitHub
- Два источника: **Personal** (свои и командные репозитории по user access token) и **Public**
  (любой публичный репозиторий по ссылке `https://github.com/owner/repo`).
- Список репозиториев подтягивается сразу после ввода токена, список веток — после выбора репозитория.
- Приложение обходит указанный путь через GitHub REST Contents API и загружает каждый найденный
  `spec.md` как отдельную подспецификацию.

```
openspec/                      .specify/
└─ specs/                      └─ specs/
   └─ dashboard/spec.md           └─ auth-flow/spec.md
```

### Поддерживаемые форматы
- **OpenSpec** — заголовки `### Requirement:` и `#### Scenario:` с шагами `GIVEN` / `WHEN` / `THEN` / `AND`,
  включая многострочные шаги.
- **spec-kit** — user stories с acceptance-сценариями, `Edge Cases` и `Functional Requirements`.

### Ревью
- Карточная колода со свайпами: вправо — «корректный», влево — «некорректный», есть отмена (undo)
  последнего решения.
- Ревью можно запустить в любом скоупе: вся спецификация, отдельная подспека или одно требование.
- Просмотренные карточки не показываются повторно; прогресс сохраняется между запусками.
- Из карточки можно открыть исходник на GitHub — ссылка ведёт на конкретную строку файла
  (`.../blob/<branch>/<path>#L<line>`).

### Статистика и отчёты
- Счётчики корректных / некорректных / неоценённых на уровне спецификации, подспеки и требования,
  фильтры по статусу и время последней оценки.
- Текстовый отчёт ревью с выбором, что включить (только некорректные или ещё и корректные),
  и системный шаринг текста или изображения карточки.

### Прочее
- Встроенная демо-спецификация и онбординг — можно попробовать флоу до подключения своего репозитория.
- Тема оформления: авто / светлая / тёмная.
- Локализация: русский и английский.

## Архитектура

| Слой | Инструменты |
| --- | --- |
| UI | Compose Multiplatform 1.9.3, Material 3, Tabler Icons, Coil |
| Навигация | Decompose 3.4.0 (`childStack`, `Config` / `Child`) |
| Состояние | MVIKotlin 4.3.0 (MVI), Essenty 2.5.0 (lifecycle, state keeper) |
| DI | Koin 4.1.1 |
| Данные | Ktor Client (GitHub API), kotlinx-serialization, kotlinx-datetime |
| Бэкенд | Firebase Auth (анонимный вход), Firestore, Analytics — через gitlive 2.4.0 |
| Логи | Kermit |

Ключевые решения:

- Весь UI и бизнес-логика живут в `commonMain`, платформенные части вынесены в `expect`/`actual`
  (шаринг, буфер обмена, открытие ссылок, WebView, хранилища).
- Промежуточный source set `mobileMain` (Android + iOS) содержит реализации на базе Firebase —
  так Firebase не протекает в общий код.
- Спецификации и статусы ревью хранятся локально в JSON: `SharedPreferences` на Android,
  `NSUserDefaults` на iOS. GitHub-токен лежит в отдельном secure storage.

## Структура проекта

```
composeApp/src/
├─ commonMain/kotlin/com/alki/specinspect/
│  ├─ data/
│  │  ├─ importer/       # GitHub REST Contents API, загрузка spec.md
│  │  ├─ openspec/       # парсер формата OpenSpec
│  │  ├─ speckit/        # парсер формата spec-kit
│  │  ├─ specification/  # сборка Specification из импортированных файлов
│  │  ├─ models/         # Specification / Subspec / Requirement / Scenario, статистика, отчёт
│  │  ├─ repository/     # Specification, Review, Auth, User
│  │  ├─ storage/        # expect-интерфейсы персистентности
│  │  ├─ analytics/      # AnalyticsLogger
│  │  └─ demo/           # встроенная демо-спецификация
│  ├─ features/          # onboarding, myspecs, addspec, spec, subspec, requirement, review, settings, webcontent
│  ├─ navigation/        # RootComponent + RootContent (Decompose)
│  ├─ ui/                # компоненты и тема
│  ├─ localization/      # ключи и ресурсы строк
│  └─ di/                # Koin-модули
├─ mobileMain/           # actual-реализации на Firebase (Android + iOS)
├─ androidMain/          # MainActivity, платформенные actual
├─ iosMain/              # MainViewController, платформенные actual
└─ commonTest/           # тесты парсеров, репозиториев, отчёта
iosApp/                  # Xcode-проект, точка входа для iOS
openspec/                # спецификации самого приложения (dogfooding)
```

## Сборка и запуск

### Требования
- JDK 17
- Android SDK: `compileSdk`/`targetSdk` 36, `minSdk` 24
- Xcode (для iOS; таргеты `iosArm64`, `iosSimulatorArm64`)
- `composeApp/google-services.json` и `iosApp/iosApp/GoogleService-Info.plist` — конфигурация Firebase

### Android

```shell
./gradlew :composeApp:assembleDebug
```

На Windows:

```shell
.\gradlew.bat :composeApp:assembleDebug
```

### iOS

Откройте [/iosApp](./iosApp) в Xcode и запустите схему `iosApp`, либо используйте run-конфигурацию
из IDE.

### Тесты

```shell
./gradlew :composeApp:allTests
```

## Приватность

- Данные ревью и импортированные спецификации хранятся на устройстве.
- GitHub-токен используется только для запросов к GitHub API и хранится в защищённом хранилище платформы.
- Firebase используется для анонимной авторизации и аналитики использования; рекламный идентификатор
  отключён (`AD_ID` удалён из манифеста).
- Политика конфиденциальности: <https://paslenstudio.github.io/SpecInspect_Policy.html>

## Обратная связь

Вопросы и предложения — paslenapp@gmail.com (в приложении: «Настройки» → «Связаться с разработчиком»).
