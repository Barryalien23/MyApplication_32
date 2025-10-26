package com.raux.myapplication_32.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.YuvImage
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raux.myapplication_32.data.models.*
import com.raux.myapplication_32.engine.ASCIIEngineV2
import com.raux.myapplication_32.engine.Grid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
* ViewModel для главного экрана приложения
 */
class MainViewModel(
    private val context: Context
) : ViewModel() {
    
    // Размер шрифта для ASCII эффекта (теперь в px, из Grid)
    private val _asciiFontSize = MutableStateFlow(24f)
    val asciiFontSize: StateFlow<Float> = _asciiFontSize.asStateFlow()
    
    // Метрики сетки (lineHeight в px)
    private val _asciiGrid = MutableStateFlow<Grid?>(null)
    val asciiGrid: StateFlow<Grid?> = _asciiGrid.asStateFlow()
    
    // Состояние камеры
    private val _cameraFacing = MutableStateFlow(CameraFacing.BACK)
    val cameraFacing: StateFlow<CameraFacing> = _cameraFacing.asStateFlow()
    
    // Состояние захвата фото
    private val _captureState = MutableStateFlow<CaptureState>(CaptureState.Idle)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()
    
    // Текущий эффект
    private val _currentEffect = MutableStateFlow(EffectType.ASCII)
    val currentEffect: StateFlow<EffectType> = _currentEffect.asStateFlow()
    
    // Параметры эффекта
    private val _effectParams = MutableStateFlow(EffectParams())
    val effectParams: StateFlow<EffectParams> = _effectParams.asStateFlow()
    
    // Цветовая схема
    private val _colorState = MutableStateFlow(ColorState())
    val colorState: StateFlow<ColorState> = _colorState.asStateFlow()
    
    // Текущее изображение
    private val _currentImage = MutableStateFlow<Bitmap?>(null)
    val currentImage: StateFlow<Bitmap?> = _currentImage.asStateFlow()
    
    // Результат ASCII конвертации
    private val _asciiResult = MutableStateFlow("")
    val asciiResult: StateFlow<String> = _asciiResult.asStateFlow()
    
    // Текущий экран
    private val _currentScreen = MutableStateFlow(Screen.MAIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    // Выбранный параметр для настроек эффекта
    private val _selectedEffectParam = MutableStateFlow<String?>(null)
    val selectedEffectParam: StateFlow<String?> = _selectedEffectParam.asStateFlow()
    
    init {
        // Инициализируем с тестовым ASCII
        generateTestASCII()
    }
    
    /**
     * Генерирует тестовый ASCII для демонстрации
     */
    private fun generateTestASCII() {
        viewModelScope.launch {
            try {
                val testBitmap = createTestBitmap()
                val effect = _currentEffect.value
                val params = _effectParams.value
                
                // Используем новый движок V2
                val result = ASCIIEngineV2.renderText(
                    source = testBitmap,
                    effectType = effect,
                    params = params,
                    screenWidthPx = 1080,  // примерное значение, будет обновлено из UI
                    screenHeightPx = 1920,
                    baseFontPx = 24f,
                    maxCells = 80_000,
                    dither = true,
                    gamma = 1.25f
                )
                
                _asciiResult.value = result.ascii
                _asciiFontSize.value = result.grid.fontPx
                _asciiGrid.value = result.grid
                
                android.util.Log.d("MainViewModel", "TestASCII: Grid ${result.grid.cols}x${result.grid.rows}, font=${result.grid.fontPx}px, clamped=${result.grid.clamped}")
            } catch (e: Exception) {
                _asciiResult.value = "ASCII Engine Error"
                android.util.Log.e("MainViewModel", "TestASCII error", e)
            }
        }
    }
    
    /**
     * Переключение камеры
     */
    fun toggleCamera() {
        _cameraFacing.value = when (_cameraFacing.value) {
            CameraFacing.BACK -> CameraFacing.FRONT
            CameraFacing.FRONT -> CameraFacing.BACK
        }
    }
    
    /**
     * Захват фото с ASCII эффектом
     */
    fun capturePhoto() {
        _captureState.value = CaptureState.Capturing
        viewModelScope.launch {
            try {
                val asciiText = _asciiResult.value
                val colorState = _colorState.value
                val fontSize = _asciiFontSize.value
                
                // Отладочная информация
                android.util.Log.d("ASCII_Capture", "ColorState: ${colorState.symbols}")
                android.util.Log.d("ASCII_Capture", "ASCII text length: ${asciiText.length}")
                
                val imageUri = saveASCIIToGallery(asciiText, colorState, fontSize)
                if (imageUri != null) {
                    _captureState.value = CaptureState.Success(imageUri)
                } else {
                    _captureState.value = CaptureState.Error("Failed to save ASCII image")
                }
            } catch (e: Exception) {
                _captureState.value = CaptureState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    /**
     * Выбор эффекта
     */
    fun selectEffect(effect: EffectType) {
        _currentEffect.value = effect
        processCurrentImage()
    }
    
    /**
     * Обновление эффекта
     */
    fun updateEffect(effect: EffectType) {
        _currentEffect.value = effect
        processCurrentImage()
    }
    
    /**
     * Обновление параметров эффекта
     */
    fun updateEffectParams(params: EffectParams) {
        _effectParams.value = params
        processCurrentImage()
    }
    
    /**
     * Обновление цветовой схемы
     */
    fun updateColorState(colorState: ColorState) {
        _colorState.value = colorState
    }
    
    /**
     * Обновление цвета символов
     */
    fun updateSymbolColor(color: Color) {
        val currentState = _colorState.value
        _colorState.value = currentState.copy(
            symbols = SymbolPaint.Solid(color)
        )
    }
    
    /**
     * Обновление градиента символов
     */
    fun updateSymbolGradient(startColor: Color, endColor: Color) {
        val currentState = _colorState.value
        _colorState.value = currentState.copy(
            symbols = SymbolPaint.Gradient(startColor, endColor)
        )
    }
    
    /**
     * Обновление цвета фона
     */
    fun updateBackgroundColor(color: Color) {
        val currentState = _colorState.value
        _colorState.value = currentState.copy(background = color)
    }
    
    /**
     * Обработка текущего изображения
     */
    private fun processCurrentImage(screenWidthPx: Int = 1080, screenHeightPx: Int = 1920) {
        val image = _currentImage.value ?: return
        val effect = _currentEffect.value
        val params = _effectParams.value
        
        viewModelScope.launch {
            try {
                // Новый движок не требует предварительного масштабирования
                // Он сам сэмплирует через интегральное изображение
                val result = ASCIIEngineV2.renderText(
                    source = image,
                    effectType = effect,
                    params = params,
                    screenWidthPx = screenWidthPx,
                    screenHeightPx = screenHeightPx,
                    baseFontPx = 24f,
                    maxCells = 80_000,
                    dither = true,
                    gamma = 1.25f
                )
                
                _asciiResult.value = result.ascii
                _asciiFontSize.value = result.grid.fontPx
                _asciiGrid.value = result.grid
                
                if (result.grid.clamped) {
                    android.util.Log.w("MainViewModel", "⚠️ Grid clamped! Consider reducing CELL parameter")
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error processing current image", e)
            }
        }
    }
    
    /**
     * Масштабирует изображение для оптимизации производительности ASCII
     */
    private fun scaleBitmapForASCII(bitmap: Bitmap, maxWidth: Int = 400, maxHeight: Int = 300): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        // Вычисляем коэффициент масштабирования
        val scaleX = maxWidth.toFloat() / originalWidth
        val scaleY = maxHeight.toFloat() / originalHeight
        val scale = minOf(scaleX, scaleY, 1f) // Не увеличиваем изображение
        
        val newWidth = (originalWidth * scale).toInt()
        val newHeight = (originalHeight * scale).toInt()
        
        // Создаем масштабированное изображение
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
        
        // Освобождаем оригинальное изображение только если оно отличается
        if (scaledBitmap != bitmap) {
            bitmap.recycle()
        }
        
        return scaledBitmap
    }
    
    /**
     * Обработка изображения с камеры в реальном времени
     * @param screenWidthPx ширина экрана в пикселях
     * @param screenHeightPx высота экрана в пикселях
     */
    fun processCameraImage(imageProxy: ImageProxy, screenWidthPx: Int, screenHeightPx: Int) {
        viewModelScope.launch {
            try {
                val bitmap = imageProxyToBitmap(imageProxy)
                if (bitmap != null) {
                    val effect = _currentEffect.value
                    val params = _effectParams.value
                    
                    // Используем новый движок V2 с реальными размерами экрана в PX
                    val result = ASCIIEngineV2.renderText(
                        source = bitmap,
                        effectType = effect,
                        params = params,
                        screenWidthPx = screenWidthPx,
                        screenHeightPx = screenHeightPx,
                        baseFontPx = 24f,
                        maxCells = 80_000,
                        dither = true,
                        gamma = 1.25f
                    )
                    
                    _asciiResult.value = result.ascii
                    _asciiFontSize.value = result.grid.fontPx
                    _asciiGrid.value = result.grid
                    
                    // Предупреждение если упёрлись в лимит
                    if (result.grid.clamped) {
                        android.util.Log.w("MainViewModel", "⚠️ Grid clamped! CELL=${params.cell}% → Grid ${result.grid.cols}x${result.grid.rows}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Error processing camera image", e)
            }
        }
    }    
    /**
     * Конвертация ImageProxy в Bitmap с ОПТИМИЗАЦИЕЙ МАСШТАБИРОВАНИЯ
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val buffer = imageProxy.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            
            val yuvImage = YuvImage(
                data,
                ImageFormat.NV21,
                imageProxy.width,
                imageProxy.height,
                null
            )
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                Rect(0, 0, imageProxy.width, imageProxy.height),
                50,
                out
            )
            val imageBytes = out.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            
            // Исправляем поворот изображения
            val matrix = Matrix()
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            matrix.postRotate(rotationDegrees.toFloat())
            
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            
            // Освобождаем память
            bitmap.recycle()
            
            // МАСШТАБИРУЕМ ИЗОБРАЖЕНИЕ ДЛЯ ASCII - увеличено для лучшей детализации!
            val maxWidth = 500  // Максимальная ширина для ASCII (было 300)
            val maxHeight = 375 // Максимальная высота для ASCII (было 225)

            val originalWidth = rotatedBitmap.width
            val originalHeight = rotatedBitmap.height

            // Вычисляем коэффициент масштабирования
            val scaleX = maxWidth.toFloat() / originalWidth
            val scaleY = maxHeight.toFloat() / originalHeight
            val scale = minOf(scaleX, scaleY, 1f) // Не увеличиваем изображение

            val newWidth = (originalWidth * scale).toInt()
            val newHeight = (originalHeight * scale).toInt()

            // Создаем масштабированное изображение
            val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, newWidth, newHeight, false)

            // Освобождаем повернутое изображение
            rotatedBitmap.recycle()

            // Возвращаем масштабированное изображение
            scaledBitmap
        } catch (e: Exception) {
            // Если не удалось конвертировать, создаем простой тестовый bitmap
            createTestBitmap()
        }
    }
    
    /**
     * Создает тестовый bitmap для демонстрации
     */
    private fun createTestBitmap(): Bitmap {
        val width = 200
        val height = 150
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // Создаем простой градиент для демонстрации
        for (x in 0 until width) {
            for (y in 0 until height) {
                val gray = ((x + y) * 255 / (width + height)).toInt()
                val color = android.graphics.Color.rgb(gray, gray, gray)
                bitmap.setPixel(x, y, color)
            }
        }
        
        return bitmap
    }
    
    /**
     * Сохранение ASCII текста как изображение в галерею
     */
    private suspend fun saveASCIIToGallery(
        asciiText: String,
        colorState: ColorState,
        fontSize: Float
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Создаем Bitmap из ASCII текста
            val bitmap = createASCIIBitmap(asciiText, colorState, fontSize)
            
            // Генерируем уникальное имя файла
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "ASCII_Art_$timestamp.jpg"
            
            // Сохраняем в галерею
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ASCII_Art")
            }
            
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let { imageUri ->
                context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                return@withContext imageUri.toString()
            }
            
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }
    
    /**
     * Создание Bitmap из ASCII текста без рамок
     */
    private fun createASCIIBitmap(
        asciiText: String,
        colorState: ColorState,
        fontSize: Float
    ): Bitmap {
        val lines = asciiText.split("\n")
        val maxLineLength = lines.maxOfOrNull { it.length } ?: 0
        val lineCount = lines.size
        
        // Оптимальный размер шрифта для сохранения (больше чем на экране)
        val saveFontSize = maxOf(fontSize * 2f, 24f) // Увеличиваем шрифт в 2 раза или минимум 24px
        
        // Вычисляем размеры изображения точно по тексту (без отступов)
        val charWidth = saveFontSize * 0.6f // Примерная ширина символа
        val charHeight = saveFontSize * 1.2f // Примерная высота символа
        
        val imageWidth = (maxLineLength * charWidth).toInt()
        val imageHeight = (lineCount * charHeight).toInt()
        
        // Создаем Bitmap точно по тексту (без отступов)
        val bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Заливаем фон
        canvas.drawColor(colorState.background.toArgb())
        
        // Рисуем ASCII текст с поддержкой градиента
        var y = saveFontSize
        for ((lineIndex, line) in lines.withIndex()) {
            // Вычисляем цвет для этой строки в градиенте
            val progress = if (lines.size > 1) lineIndex.toFloat() / (lines.size - 1) else 0f
            
            val lineColor = when (val symbols = colorState.symbols) {
                is SymbolPaint.Solid -> symbols.color.toArgb()
                is SymbolPaint.Gradient -> {
                    val startColor = symbols.start
                    val endColor = symbols.end
                    // Правильная конвертация Compose Color в Android Color
                    val red = ((startColor.red + (endColor.red - startColor.red) * progress) * 255).toInt().coerceIn(0, 255)
                    val green = ((startColor.green + (endColor.green - startColor.green) * progress) * 255).toInt().coerceIn(0, 255)
                    val blue = ((startColor.blue + (endColor.blue - startColor.blue) * progress) * 255).toInt().coerceIn(0, 255)
                    
                    // Отладочная информация
                    android.util.Log.d("ASCII_Gradient", "Line $lineIndex: progress=$progress, RGB=($red, $green, $blue)")
                    
                    android.graphics.Color.rgb(red, green, blue)
                }
            }
            
            // Настраиваем Paint для этой строки
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = saveFontSize
                typeface = Typeface.MONOSPACE
                color = lineColor
            }
            
            canvas.drawText(line, 0f, y, textPaint)
            y += saveFontSize * 1.2f
        }
        
        return bitmap
    }
    
    /**
     * Навигация между экранами
     */
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
    
    fun navigateToEffectSettings(selectedParam: String) {
        _selectedEffectParam.value = selectedParam
        _currentScreen.value = Screen.EFFECT_SETTINGS
    }
    
    fun navigateToColorPicker(colorType: String) {
        _selectedEffectParam.value = colorType
        _currentScreen.value = Screen.COLOR_PICKER
    }
    
    fun navigateBack() {
        _currentScreen.value = Screen.MAIN
        _selectedEffectParam.value = null // Reset selected param on back
    }
}

/**
 * Экраны приложения
 */
enum class Screen {
    MAIN,
    EFFECT_PICKER,
    EFFECT_SETTINGS,
    COLOR_PICKER
}
