package com.raux.myapplication_32.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raux.myapplication_32.data.models.*
import com.raux.myapplication_32.engine.ASCIIEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

/**
 * ViewModel для главного экрана приложения
 */
class MainViewModel : ViewModel() {
    
    private val asciiEngine = ASCIIEngine()
    
    // Размер шрифта для ASCII эффекта
    private val _asciiFontSize = MutableStateFlow(16f)
    val asciiFontSize: StateFlow<Float> = _asciiFontSize.asStateFlow()
    
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
                val (asciiText, optimalFontSize) = asciiEngine.convertToASCII(testBitmap, effect, params, 300, 200, 1f)
                _asciiResult.value = asciiText
                _asciiFontSize.value = optimalFontSize
            } catch (e: Exception) {
                _asciiResult.value = "ASCII Engine Error"
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
     * Захват фото
     */
    fun capturePhoto() {
        _captureState.value = CaptureState.Capturing
        // Здесь будет логика захвата фото
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000) // Имитация процесса захвата
            _captureState.value = CaptureState.Idle
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
    private fun processCurrentImage() {
        val image = _currentImage.value ?: return
        val effect = _currentEffect.value
        val params = _effectParams.value
        
        viewModelScope.launch {
            try {
                // Масштабируем изображение для оптимизации
                val scaledImage = scaleBitmapForASCII(image, maxWidth = 200, maxHeight = 150)
                
                val (asciiText, optimalFontSize) = asciiEngine.convertToASCII(scaledImage, effect, params, 300, 200, 1f)
                _asciiResult.value = asciiText
                _asciiFontSize.value = optimalFontSize
                
                // Освобождаем память
                if (scaledImage != image) {
                    scaledImage.recycle()
                }
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }
    
    /**
     * Масштабирует изображение для оптимизации производительности ASCII
     */
    private fun scaleBitmapForASCII(bitmap: Bitmap, maxWidth: Int = 200, maxHeight: Int = 150): Bitmap {
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
     */
    fun processCameraImage(imageProxy: ImageProxy, screenWidth: Int = 300, screenHeight: Int = 200, fontSize: Float = 1f) {
        viewModelScope.launch {
            try {
                val bitmap = imageProxyToBitmap(imageProxy)
                if (bitmap != null) {
                    val effect = _currentEffect.value
                    val params = _effectParams.value
                    
                    val (asciiText, optimalFontSize) = asciiEngine.convertToASCII(bitmap, effect, params, screenWidth, screenHeight, fontSize)
                    _asciiResult.value = asciiText
                    _asciiFontSize.value = optimalFontSize
                }
            } catch (e: Exception) {
                // Обработка ошибок
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
            
            // МАСШТАБИРУЕМ ИЗОБРАЖЕНИЕ ДЛЯ ASCII - это ключевая оптимизация!
            val maxWidth = 200  // Максимальная ширина для ASCII
            val maxHeight = 150 // Максимальная высота для ASCII

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
     * Навигация между экранами
     */
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }
    
    fun navigateToEffectSettings(selectedParam: String) {
        _selectedEffectParam.value = selectedParam
        _currentScreen.value = Screen.EFFECT_SETTINGS
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
