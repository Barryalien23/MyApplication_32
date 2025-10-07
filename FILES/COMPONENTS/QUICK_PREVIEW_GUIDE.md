# 🎨 Быстрый просмотр компонентов

## Способ 1: Compose Preview в Android Studio (РЕКОМЕНДУЕТСЯ)

### Шаги:
1. **Откройте Android Studio**
2. **Создайте новый проект** с Compose Activity
3. **Скопируйте файлы:**
   - Всю папку `ui/` → `src/main/java/com/digitalreality/ui/`
   - Всю папку `data/` → `src/main/java/com/digitalreality/data/`
   - Всю папку `res/` → `src/main/res/` (иконки уже готовы!)
   - `build.gradle.kts` → замените содержимое своего `build.gradle.kts`

4. **Откройте файлы превью:**
   - `ui/preview/ComponentPreviews.kt`
   - `ui/preview/ScreenPreviews.kt`

5. **В Android Studio справа появится панель "Preview"** - там вы увидите все компоненты!

### Что вы увидите:
- ✅ **Capture Button** - большая белая кнопка фото
- ✅ **Function Buttons** - кнопки загрузки/поворота камеры **с настоящими иконками!**
- ✅ **Effect Button** - кнопка ASCII эффекта **с оригинальной иконкой из Figma**
- ✅ **Setting Buttons** - кнопки параметров **с правильными иконками и прогресс-индикаторами**
- ✅ **Color Buttons** - кнопки выбора цветов
- ✅ **Sliders** - слайдеры с числовыми значениями **и иконками параметров**
- ✅ **Complete Screens** - полные экраны **с всеми иконками из вашего дизайна**

## Способ 2: Полное приложение

### Если хотите увидеть все в действии:
1. Следуйте `INTEGRATION_GUIDE.md`
2. Добавьте иконки из `ICONS_NEEDED.md`
3. Скопируйте шрифты IBM Plex Mono
4. Запустите на эмуляторе

## Способ 3: Статические скриншоты

В вашей папке `SCREENS/SCREENSHOTS/` уже есть скриншоты дизайна из Figma:
- `1.Default screen.png` - главный экран
- `2.Effect selection.png` - выбор эффекта
- `4.Effect Settings.png` - настройки параметров
- `7.Background color selection.png` - выбор цветов

Мои компоненты точно повторяют этот дизайн!

## Особенности Preview

### В ComponentPreviews.kt вы увидите:
```kotlin
@Preview(name = "Capture Button")
@Composable
fun PreviewCaptureButton() {
    // Показывает кнопку в обычном и активном состоянии
}

@Preview(name = "Setting Buttons") 
@Composable
fun PreviewSettingButtons() {
    // Показывает все 4 кнопки параметров с разными значениями прогресса
}
```

### В ScreenPreviews.kt:
```kotlin
@Preview(name = "Main Screen Layout")
@Composable  
fun PreviewMainScreenLayout() {
    // Показывает полный макет главного экрана
}
```

## Что делать, если Preview не работает

1. **Sync Project** (Ctrl+Shift+O / Cmd+Shift+O)
2. **Build → Clean Project**
3. **Build → Rebuild Project** 
4. Убедитесь, что все зависимости из `build.gradle.kts` добавлены

## Цвета и стили

Все компоненты используют:
- 🎨 **Цвета из Figma** (AppColors.MainGrey, AppColors.White, etc.)
- 🔤 **Шрифт IBM Plex Mono** (AppTypography.body1, body2, head1)
- 📏 **Размеры из Variables** (Spacing, Roundings, ComponentSizes)
- ✨ **Эффекты** (blur, shadow, прозрачности)

Все в точности как в вашем дизайне! 🎯
