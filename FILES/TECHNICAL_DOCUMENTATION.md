# 🔧 ASCII Camera App - Техническая документация

## 📊 Структура данных

### **EffectParams**
```kotlin
data class EffectParams(
    val cell: Int = 50,      // 0-100, управляет количеством символов
    val jitter: Int = 0,     // 0-100, добавляет случайные вариации
    val softy: Int = 0       // 0-100, применяет размытие
)
```

### **ColorState**
```kotlin
data class ColorState(
    val background: Color = Color.Black,
    val symbols: SymbolPaint = SymbolPaint.Solid(Color.White)
)

sealed class SymbolPaint {
    data class Solid(val color: Color) : SymbolPaint()
    data class Gradient(val start: Color, val end: Color) : SymbolPaint()
}
```

### **CameraFacing**
```kotlin
enum class CameraFacing {
    FRONT, BACK
}
```

### **CaptureState**
```kotlin
sealed class CaptureState {
    object Idle : CaptureState()
    object Capturing : CaptureState()
}
```

---

## 🎯 Алгоритмы ASCII Engine

### **1. Масштабирование изображения**
```kotlin
private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    // Конвертация YUV в JPEG
    val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
    
    // Исправление поворота
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees.toFloat())
    
    // Масштабирование для производительности
    val maxWidth = 200
    val maxHeight = 150
    val scale = minOf(scaleX, scaleY, 1f)
    
    return Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, false)
}
```

### **2. Расчет количества символов**
```kotlin
fun convertToASCII(...): Pair<String, Float> {
    // Умный расчет на основе размера экрана
    val maxCharsByScreenWidth = (screenWidth / 4f).toInt()
    val maxCharsByScreenHeight = (screenHeight / 4f).toInt()
    
    // CELL управляет количеством символов
    val cellPercent = params.cell / 100f
    val charsPerRow = (minSymbolsWidth + (maxSymbolsWidthCalculated - minSymbolsWidth) * cellPercent).toInt()
    val rowsCount = (minSymbolsHeight + (maxSymbolsHeightCalculated - minSymbolsHeight) * cellPercent).toInt()
    
    // Расчет оптимального размера шрифта
    val optimalFontSize = calculateOptimalFontSize(charsPerRow, rowsCount, screenWidth, screenHeight)
    
    return Pair(asciiText, optimalFontSize)
}
```

### **3. Применение эффектов**

#### **JITTER эффект:**
```kotlin
private fun applyJitter(char: Char, jitter: Int): Char {
    if (jitter == 0) return char
    
    val jitterAmount = jitter / 20
    val randomOffset = (Math.random() * jitterAmount * 2 - jitterAmount).toInt()
    
    val chars = asciiChars
    val currentIndex = chars.indexOf(char)
    val newIndex = (currentIndex + randomOffset).coerceIn(0, chars.length - 1)
    
    return chars[newIndex]
}
```

#### **SOFTY эффект:**
```kotlin
private fun applyBlur(bitmap: Bitmap, intensity: Int): Bitmap {
    val radius = intensity / 10f
    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    // Здесь можно добавить более сложный алгоритм размытия 
    return result
}
```

---

## 🎨 UI Компоненты

### **1. ASCIIPreview**
```kotlin
@Composable
fun ASCIIPreview(
    asciiText: String,
    backgroundColor: Color,
    textColor: Color,
    fontSize: Float = 16f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = asciiText,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Start, // Заполнение всей ширины
            lineHeight = (fontSize * 1.1f).sp,
            maxLines = Int.MAX_VALUE
        )
    }
}
```

### **2. MainSettingButton с прогресс-баром**
```kotlin
@Composable
private fun MainSettingButton(
    paramName: String,
    icon: ImageVector,
    value: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .clickable { onClick() }
    ) {
        // Вертикальная полоска прогресса
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(value / 100f)
                .background(Color(0xFF252525))
                .align(Alignment.CenterStart)
        )
        
        // Иконка и текст по центру
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = paramName)
            Text(text = paramName)
        }
    }
}
```

### **3. Интерактивный слайдер**
```kotlin
@Composable
fun EffectParameterSlider(
    paramName: String,
    icon: ImageVector,
    value: Int,
    onValueChange: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    var lastUpdateTime by remember { mutableStateOf(0L) }
    val animatedProgress by animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = if (isDragging) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 150, easing = FastOutSlowInEasing)
        }
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.MainGrey)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newValue = ((offset.x / size.width) * 100).toInt().coerceIn(0, 100)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime >= 16) { // 60fps для тапов
                        onValueChange(newValue)
                        lastUpdateTime = currentTime
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val newValue = ((change.position.x / size.width) * 100).toInt().coerceIn(0, 100)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime >= 32) { // 30fps для перетаскивания
                        onValueChange(newValue)
                        lastUpdateTime = currentTime
                    }
                }
            }
    ) {
        // Прогресс-бар
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (isDragging) value / 100f else animatedProgress)
                .background(Color(0xFF252525))
        )
        
        // Контент слайдера
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$value%")
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack)
            }
        }
    }
}
```

---

## 🔄 Потоки данных

### **StateFlow в MainViewModel**
```kotlin
class MainViewModel : ViewModel() {
    // Состояние камеры
    private val _cameraFacing = MutableStateFlow(CameraFacing.BACK)
    val cameraFacing: StateFlow<CameraFacing> = _cameraFacing.asStateFlow()
    
    // Параметры эффекта
    private val _effectParams = MutableStateFlow(EffectParams())
    val effectParams: StateFlow<EffectParams> = _effectParams.asStateFlow()
    
    // ASCII результат
    private val _asciiResult = MutableStateFlow("")
    val asciiResult: StateFlow<String> = _asciiResult.asStateFlow()
    
    // Размер шрифта
    private val _asciiFontSize = MutableStateFlow(16f)
    val asciiFontSize: StateFlow<Float> = _asciiFontSize.asStateFlow()
}
```

### **Сборка состояния в UI**
```kotlin
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val cameraFacing by viewModel.cameraFacing.collectAsStateWithLifecycle()
    val effectParams by viewModel.effectParams.collectAsStateWithLifecycle()
    val asciiResult by viewModel.asciiResult.collectAsStateWithLifecycle()
    val asciiFontSize by viewModel.asciiFontSize.collectAsStateWithLifecycle()
    
    // UI компоненты
}
```

---

## ⚡ Оптимизация производительности

### **1. Масштабирование изображений**
- Изображения масштабируются до 200x150 пикселей перед ASCII обработкой
- Это уменьшает количество данных в 10-100 раз

### **2. Ограничение символов**
- Максимум 200x150 символов для предотвращения зависаний
- Адаптивное количество символов на основе размера экрана

### **3. Throttling обновлений**
- Слайдер: 30fps для перетаскивания, 60fps для тапов
- Ограничение частоты вызовов `onValueChange`

### **4. Управление памятью**
```kotlin
// Автоматическое освобождение bitmap'ов
if (scaledImage != image) {
    scaledImage.recycle()
}
bitmap.recycle()
rotatedBitmap.recycle()
```

---

## 🎯 Навигация

### **Экраны приложения**
```kotlin
enum class Screen {
    MAIN,              // Главный экран с ASCII-эффектом
    EFFECT_PICKER,     // Выбор типа эффекта
    EFFECT_SETTINGS,   // Настройка параметров эффекта
    COLOR_PICKER       // Выбор цветов
}
```

### **Навигационные функции**
```kotlin
fun navigateTo(screen: Screen) {
    _currentScreen.value = screen
}

fun navigateToEffectSettings(selectedParam: String) {
    _selectedEffectParam.value = selectedParam
    _currentScreen.value = Screen.EFFECT_SETTINGS
}

fun navigateBack() {
    _currentScreen.value = Screen.MAIN
    _selectedEffectParam.value = null
}
```

---

## 🎨 Анимации

### **Микро-анимации**
```kotlin
// Анимация нажатия кнопки
fun Modifier.buttonPressAnimation(interactionSource: MutableInteractionSource): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
    )
    
    return this.graphicsLayer(scaleX = scale, scaleY = scale)
}

// Анимация слайдера
fun Modifier.sliderAnimation(): Modifier {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)
    )
    
    return this.graphicsLayer(scaleX = scale, scaleY = scale)
}
```

---

## 📱 Разрешения и камера

### **AndroidManifest.xml**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<uses-feature android:name="android.hardware.camera" android:required="true" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
```

### **CameraPreview компонент**
```kotlin
@Composable
fun CameraPreview(
    cameraFacing: CameraFacing,
    modifier: Modifier = Modifier,
    onImageAnalysis: (ImageProxy) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    LaunchedEffect(cameraFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        val cameraSelector = when (cameraFacing) {
            CameraFacing.FRONT -> CameraSelector.DEFAULT_FRONT_CAMERA
            CameraFacing.BACK -> CameraSelector.DEFAULT_BACK_CAMERA
        }
        
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(cameraExecutor, ImageAnalyzer(onImageAnalysis)) }
        
        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
    }
}
```

---

## 🔍 Отладка и мониторинг

### **Логирование производительности**
```kotlin
// В ASCIIEngine
private fun convertToASCII(...): Pair<String, Float> {
    val startTime = System.currentTimeMillis()
    
    // Обработка изображения
    val result = processImage(...)
    
    val endTime = System.currentTimeMillis()
    Log.d("ASCIIEngine", "Processing time: ${endTime - startTime}ms")
    
    return result
}
```

### **Мониторинг памяти**
```kotlin
// Проверка размера bitmap'а
Log.d("Memory", "Bitmap size: ${bitmap.width}x${bitmap.height}")
Log.d("Memory", "Bitmap config: ${bitmap.config}")

// Освобождение памяти
bitmap.recycle()
```

---

*Техническая документация создана: $(date)*
*Версия: 1.0*
