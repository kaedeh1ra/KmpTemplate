# KMP Template

Шаблон Kotlin Multiplatform + Compose Multiplatform: Android-приложение и веб (Kotlin/Wasm) из одного
кода. Внутри — модульная архитектура, MVI, Koin, навигация через плагины фич, Ktor-клиент и генератор
новых фич. Пакет по умолчанию `com.example.app`, переименовывается одной командой.

## Что внутри

| Модуль                  | Назначение                                                                                                        |
|-------------------------|-------------------------------------------------------------------------------------------------------------------|
| `app/androidApp`        | Точка входа Android: `MainActivity`, манифест, подпись релиза                                                     |
| `app/webApp`            | Точка входа веба (Kotlin/Wasm): `main.kt`, `index.html` с экраном загрузки                                        |
| `app/shared`            | Оболочка: тема, верхняя панель, нижняя навигация, `NavHost` из плагинов фич, запуск Koin                          |
| `app/core/domain`       | Чистый Kotlin: модели, интерфейсы репозиториев, use case'ы                                                        |
| `app/core/data`         | Реализации репозиториев, Ktor `HttpClient`, хранилище настроек (SharedPreferences / localStorage)                 |
| `app/core/mvi`          | `MviViewModel` (State / Intent / Effect), `LoadState`, `CollectEffects`                                           |
| `app/core/navigation`   | Контракты между фичами: `FeaturePlugin`, `AppNavigator`, общие маршруты                                           |
| `app/core/designsystem` | `AppTheme` (светлая и тёмная), состояния загрузки и ошибки, `AppCard`, `WindowSize`                               |
| `app/core/testing`      | Фейки для тестов                                                                                                  |
| `app/feature/home`      | Пример фичи: экран-счётчик, MVI, use case'ы, тест ViewModel                                                       |
| `build-logic`           | Convention-плагины, автоподключение фич, задача `newFeature` — см. [build-logic/README.md](build-logic/README.md) |

Стек: Kotlin 2.4, Compose Multiplatform 1.12, AGP 9, Gradle 9.8, Koin 4, Ktor 3, Navigation Compose.
Версии — в [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

Таргеты: Android (minSdk 31) и браузер (Wasm). iOS и десктопа нет; скрипт переименования уже умеет
обрабатывать `Info.plist`, `*.pbxproj`, `*.xcconfig` и папки с `KmpTemplate` в имени, если вы их добавите.

## Как начать проект из шаблона

1. На GitHub: **Use this template → Create a new repository**.
   (Чтобы кнопка появилась у самого шаблона: Settings → General → отметить **Template repository**.)
2. Переименуйте проект — один из вариантов ниже.
3. Перепишите этот README под свой проект и удалите `rename.sh`, `rename.ps1`,
   `.github/workflows/rename.yml`: после переименования они не нужны.

## Переименование

Скрипт меняет:

- `package` / `import` и все упоминания `com.example.app` в коде и скриптах сборки;
- папки `com/example/app` → `ru/myname/coolapp` в каждом source set (`src/*/kotlin`, включая `build-logic`);
- `Constants.BASE_PACKAGE`, а от него — Android `namespace` всех модулей и `applicationId`;
- `rootProject.name` в `settings.gradle.kts` (`KmpTemplate` → ваше имя);
- отображаемое имя приложения: `strings.xml` (Android), `<title>` в `index.html` (веб).

`README.md`, `.github/` и сами скрипты не трогаются. Скрипт откажется работать, если проект уже
переименован или в git есть незакоммиченные изменения (флаг `--force` / `-Force` — всё равно
выполнить). Проверки ввода: пакет — минимум два сегмента из `[a-z0-9_]`, без ключевых слов Kotlin/Java;
имя — латинские буквы и цифры; в отображаемом имени нельзя `< > & " '`.

**macOS / Linux / Git Bash** (нужны bash и perl, оба есть в Git for Windows):

```bash
./rename.sh --package ru.myname.coolapp --name CoolApp --display-name "Cool App"
```

**Windows PowerShell:**

```powershell
.\rename.ps1 -Package ru.myname.coolapp -Name CoolApp -DisplayName "Cool App"
```

**Прямо на GitHub:** Actions → **Rename project** → Run workflow, ввести пакет и имя. Workflow запустит
`rename.sh` и закоммитит результат в выбранную ветку. В репозитории-шаблоне он не запускается.
Если Actions не может пушить, включите Settings → Actions → General → Workflow permissions → **Read and write**.

`--display-name` необязателен, по умолчанию совпадает с `--name`. После переименования:

```bash
./gradlew clean testAndroidHostTest
```

## Сборка и запуск

Нужны JDK 21+ и Android SDK (путь в `local.properties` → `sdk.dir`, Android Studio создаёт его сама).
JDK для демона Gradle задана в `gradle/gradle-daemon-jvm.properties`.

```bash
./gradlew :app:androidApp:installDebug                 # Android: собрать и поставить на устройство/эмулятор
./gradlew :app:androidApp:assembleRelease              # Android: релизный APK (подпись — ниже)
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun      # веб: dev-сервер с автооткрытием браузера
./gradlew :app:webApp:wasmJsBrowserDistribution        # веб: статика в app/webApp/build/dist/wasmJs/productionExecutable
./gradlew testAndroidHostTest                          # тесты всех модулей на JVM
```

На Windows — `gradlew.bat` или `.\gradlew` в PowerShell.

### Адрес API

`HttpClient` уже лежит в Koin (`dataModule`): репозиторий берёт его через `get()` и ходит по
относительным путям. Базовый адрес:

- веб — origin страницы (`https://ваш-сайт/`);
- Android — gradle-свойство `apiBaseUrl`, по умолчанию `https://example.com/`:
  `./gradlew :app:androidApp:installDebug -PapiBaseUrl=http://10.0.2.2:8080/` или строка
  `apiBaseUrl=...` в `gradle.properties`.

### Подпись релиза Android

Скопируйте `keystore.properties.example` в `keystore.properties` (он в `.gitignore`) и заполните, либо
задайте переменные окружения `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Без
ключа релиз подписывается debug-ключом.

## Новая фича

```bash
./gradlew newFeature --name=settings --tab="Настройки"
```

Создаст модуль `app/feature/settings` с MVI-экраном, Koin-модулем и тестом; settings, Koin и навигация
подхватят его сами. Подробности — в [build-logic/README.md](build-logic/README.md).

## CI

`.github/workflows/ci.yml` на каждый push и PR гоняет тесты, собирает debug APK и веб-дистрибутив. В
репозитории-шаблоне дополнительно проверяется, что после `rename.sh` проект собирается.
