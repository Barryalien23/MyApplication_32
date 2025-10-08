# 📸 ASCII Camera App

<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="ASCII Camera App" width="120" height="120">
  
  **Преобразуйте изображения с камеры в ASCII-арт в реальном времени**
  
  [![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![CameraX](https://img.shields.io/badge/CameraX-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/training/camerax)
</div>

---

## 🎯 О проекте

**ASCII Camera App** - это современное Android приложение, которое использует камеру устройства для создания ASCII-арт в реальном времени. Приложение сочетает в себе технологии компьютерного зрения, обработки изображений и современного UI дизайна.

### ✨ Основные возможности

- 📱 **Реальное время** - ASCII-арт обновляется мгновенно с камеры
- 🎨 **Настраиваемые эффекты** - 6 различных типов эффектов (ASCII, Shapes, Circles, Squares, Triangle, Diamonds)
- 🎛️ **Интерактивные параметры** - настройка Cell, Jitter, Softy с помощью слайдеров
- 🌈 **Цветовые схемы** - настройка фона и символов, поддержка градиентов
- 📸 **Сохранение в галерею** - высококачественные изображения без рамок
- 🔄 **Переключение камер** - фронтальная и задняя камера
- 📤 **Загрузка изображений** - работа с файлами из галереи

---

## 🚀 Скриншоты

<div align="center">
  <img src="screenshots/main_screen.png" alt="Главный экран" width="200">
  <img src="screenshots/effect_settings.png" alt="Настройки эффектов" width="200">
  <img src="screenshots/saved_gallery.png" alt="Сохраненные изображения" width="200">
</div>

---

## 🛠️ Технологии

### Основной стек
- **Kotlin** - основной язык программирования
- **Jetpack Compose** - современный UI фреймворк
- **CameraX** - работа с камерой
- **ViewModel** - управление состоянием
- **StateFlow** - реактивные потоки данных

### Архитектура
- **Clean Architecture** - разделение на слои
- **MVVM Pattern** - Model-View-ViewModel
- **Repository Pattern** - работа с данными
- **MVI Pattern** - управление состоянием и событиями

### Дополнительные библиотеки
- **Material Design 3** - современный дизайн
- **Navigation Component** - навигация между экранами
- **ViewBinding** - привязка представлений
- **Haptic Feedback** - тактильная обратная связь

---

## 📦 Установка

### Требования
- Android 7.0 (API level 24) или выше
- Камера устройства
- Разрешения на доступ к камере и галерее

### Клонирование репозитория
```bash
git clone https://github.com/yourusername/ascii-camera-app.git
cd ascii-camera-app
```

### Сборка проекта
```bash
./gradlew assembleDebug
```

### Установка на устройство
```bash
./gradlew installDebug
```

---

## 🎮 Использование

### Основные функции

1. **📸 Создание ASCII-арт**
   - Откройте приложение
   - Наведите камеру на объект
   - ASCII-арт генерируется автоматически

2. **🎛️ Настройка эффектов**
   - Нажмите на кнопку эффекта
   - Выберите тип эффекта (ASCII, Shapes, Circles, etc.)
   - Настройте параметры Cell, Jitter, Softy

3. **🌈 Изменение цветов**
   - Нажмите на кнопки цвета
   - Выберите цвет фона или символов
   - Используйте градиенты для символов

4. **📸 Сохранение изображений**
   - Нажмите центральную кнопку захвата
   - Изображение сохранится в галерею
   - Высокое качество без рамок

### Горячие клавиши
- **Центральная кнопка** - захват и сохранение фото
- **Левая кнопка** - загрузка изображения из галереи
- **Правая кнопка** - переключение между камерами

---

## 🏗️ Архитектура проекта

```
app/src/main/java/com/raux/myapplication_32/
├── 📁 data/models/           # Модели данных
│   ├── CameraModels.kt       # Модели камеры
│   ├── ColorModels.kt        # Цветовые модели
│   └── EffectModels.kt       # Модели эффектов
├── 🔧 engine/               # ASCII движок
│   └── ASCIIEngine.kt       # Основная логика конвертации
├── 🎨 ui/                   # Пользовательский интерфейс
│   ├── components/          # UI компоненты
│   │   ├── Buttons.kt       # Кнопки (Capture, Function)
│   │   ├── CameraButtonsBlock.kt
│   │   ├── MainSettingsPanel.kt
│   │   ├── EffectSettings.kt
│   │   └── Sliders.kt       # Интерактивные слайдеры
│   ├── screens/            # Экраны приложения
│   │   └── MainScreen.kt   # Главный экран
│   ├── theme/              # Тема и стили
│   │   ├── Colors.kt       # Цветовая палитра
│   │   └── Typography.kt   # Типографика
│   └── animations/         # Анимации
│       └── Animations.kt   # Микроанимации
├── 🧠 viewmodel/           # ViewModel для управления состоянием
│   └── MainViewModel.kt    # Основная логика приложения
└── 📱 MainActivity.kt      # Главная активность
```

---

## ⚙️ Настройка параметров

### Параметры эффектов

| Параметр | Описание | Диапазон | Влияние |
|----------|----------|----------|---------|
| **Cell** | Размер сетки | 0-100 | Контролирует количество символов и размер шрифта |
| **Jitter** | Скорость анимации | 0-100 | Добавляет случайные вариации к символам |
| **Softy** | Размытие/смягчение | 0-100 | Смягчает переходы между символами |

### Цветовые схемы

- **Фон** - сплошной цвет фона
- **Символы** - цвет или градиент для ASCII символов
- **Градиенты** - линейные градиенты для символов

---

## 🔧 Разработка

### Настройка среды разработки

1. **Android Studio** - последняя версия
2. **JDK 11+** - для компиляции Kotlin
3. **Android SDK** - API level 24+

### Структура кода

```kotlin
// Пример использования ASCIIEngine
val asciiEngine = ASCIIEngine()
val (asciiText, fontSize) = asciiEngine.convertToASCII(
    bitmap = imageBitmap,
    effect = EffectType.ASCII,
    params = EffectParams(cell = 50, jitter = 25, softy = 25),
    screenWidth = 300,
    screenHeight = 200,
    fontSize = 1f
)
```

### Тестирование

```bash
# Запуск unit тестов
./gradlew test

# Запуск UI тестов
./gradlew connectedAndroidTest
```

---

## 📈 Производительность

### Оптимизации

- **Масштабирование изображений** - уменьшение до 200×150px для ASCII
- **Throttling** - ограничение обновлений до 30-60 FPS
- **Кэширование** - сохранение промежуточных результатов
- **Асинхронная обработка** - использование корутин

### Рекомендации

- Используйте на устройствах с минимум 2GB RAM
- Закройте другие приложения для лучшей производительности
- Используйте заднюю камеру для лучшего качества

---

## 🤝 Вклад в проект

Мы приветствуем вклад в развитие проекта! Вот как вы можете помочь:

### Как внести вклад

1. **Fork** репозитория
2. Создайте **feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit** изменения (`git commit -m 'Add some AmazingFeature'`)
4. **Push** в branch (`git push origin feature/AmazingFeature`)
5. Откройте **Pull Request**

### Правила кодирования

- Следуйте [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Используйте осмысленные имена переменных и функций
- Добавляйте комментарии к сложной логике
- Покрывайте код тестами

---

## 📄 Лицензия

Этот проект распространяется под лицензией MIT. См. файл `LICENSE` для подробностей.

---

## 👨‍💻 Автор

**Barry** - [@yourusername](https://github.com/yourusername)

---

## 🙏 Благодарности

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - за современный UI фреймворк
- [CameraX](https://developer.android.com/training/camerax) - за простую работу с камерой
- [Material Design](https://material.io/) - за дизайн-систему
- Сообществу Android разработчиков за вдохновение

---

## 📞 Поддержка

Если у вас есть вопросы или предложения:

- 🐛 **Баг-репорты** - [Issues](https://github.com/yourusername/ascii-camera-app/issues)
- 💡 **Предложения** - [Discussions](https://github.com/yourusername/ascii-camera-app/discussions)
- 📧 **Email** - your.email@example.com

---

<div align="center">
  <p>Сделано с ❤️ для сообщества Android разработчиков</p>
  <p>⭐ Поставьте звезду, если проект вам понравился!</p>
</div>
