Technology Stack

### Core Framework
- **Kotlin Multiplatform** (v2.2.0) - Cross-platform code sharing
- **Compose Multiplatform** (v1.9.3) - UI framework for Android and iOS

### Architecture & State Management
- **Decompose** (v3.4.0) - Component library and navigation
- **MVIKotlin** (v4.3.0) - MVI pattern implementation (mandatory)
- **Essenty** (v2.5.0) - Lifecycle and state management

### Persistence & Data
- **Firebase Auth** (v2.4.0 via gitlive) - Anonymous authentication
- **Firebase Firestore** (v2.4.0 via gitlive) - All data storage

### Dependencies & DI
- **Koin** (v4.1.1) - Dependency injection
- **Kotlinx Serialization** (v1.9.0) - JSON serialization

### UI & Utilities
- **Tabler Icons** (compose.icons.TablerIcons) - Icon set
- **Coil** (v3.3.0) - Image loading
- **Kotlinx DateTime** (v0.7.1) - Date/time handling

### Основные правила:
1. **ЗАПРЕТ НА ФОРМАТИРОВАНИЕ(formatting)**: Строго запрещено менять отступы, whitespace, пробелы
   или переносы строк, если это не требуется для работы кода.
2. **Минимальные изменения (Minimal Diffs)**: При редактировании меняй только те строки, которые
   необходимы для решения задачи. Не трогай остальной код.
3. **Сохранение стиля**: Следуй текущему стилю кода в файле (именование переменных, кавычки и т.д.).
4. **Комментарии**: Если добавляешь новый сложный код, пиши комментарии на русском языке.
5. **Библиотеки**: Используй только те библиотеки, которые уже добавлены в проект. Если необходимой
   библиотеки для выполнения задачи нет, то явно спроси можно ли добавить нужную библиотеку,
   предложи варианты.
6. **Важные примечания**:
    - правильный импорт для импорт для collectAsState - import
      androidx.compose.runtime.collectAsState.
    - для Clock.System.now() правильный импорт - kotlin.time.Clock
    - Для иконок используй compose.icons.TablerIcons
    - Для coroutineScope чаще всего подходит private val scope = CoroutineScope(SupervisorJob() +
      Dispatchers.IO), если только не нужно использовать что то специфичное.
7. **Паттерны и фреймворки:**
    - MVI - При изменении кода всегда учитывать существующую архитектуру MVI + Decompose + Essenty;
      не вводить параллельные паттерны (MVP/MVVM и т.п.) без явного запроса.
    - Jetpack Compose - Избегать тяжелых операций в Compose composable; выносить их в MVI слой или
      фоновые корутины.
    - Для Room и сетевых запросов использовать подходящие диспетчеры (IO) и не блокировать
      main‑поток
    - Сериализация и сохранение состояния, essenty - для serialization использовать
      kotlinx-serialization