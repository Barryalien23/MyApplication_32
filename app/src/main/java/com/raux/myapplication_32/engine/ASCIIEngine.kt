package com.raux.myapplication_32.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.raux.myapplication_32.data.models.EffectParams
import com.raux.myapplication_32.data.models.EffectType
import kotlin.math.*

/**
 * Движок для конвертации изображений в ASCII и другие эффекты
 */
class ASCIIEngine {
    
        // ASCII символы от самых темных к самым светлым (улучшенный набор для плотности)
        private val asciiChars = " .'`^\",:;Il!i><~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$"
    
    // Символы для разных эффектов
    private val shapeChars = mapOf(
        EffectType.ASCII to asciiChars.toList().map { it.toString() },
        EffectType.SQUARES to listOf(" ", "▢", "▣", "▤", "▥", "▦", "▧", "▨", "▩", "█"),
        EffectType.CIRCLES to listOf(" ", "○", "◔", "◑", "◕", "●", "◉", "◎", "◯", "●"),
        EffectType.TRIANGLE to listOf(" ", "△", "▲", "▴", "▾", "◀", "▶", "◁", "▷", "▲"),
        EffectType.DIAMONDS to listOf(" ", "◇", "◆", "◈", "◊", "♦", "♢", "♠", "♣", "♥"),
        EffectType.SHAPES to listOf(" ", "▢", "○", "△", "◇", "▣", "◔", "▲", "◆", "█")
    )
    
    // Счетчик для демонстрации изменений (отключен для производительности)
    // private var frameCounter = 0
    
    /**
     * Конвертирует Bitmap в ASCII строку с адаптивным размером
     * CELL параметр управляет количеством символов и размером шрифта
     */
    fun convertToASCII(
        bitmap: Bitmap,
        effectType: EffectType,
        params: EffectParams,
        screenWidth: Int = 300,
        screenHeight: Int = 200,
        fontSize: Float = 1f
    ): Pair<String, Float> {
        // Ограничения для производительности (увеличили для лучшего заполнения)
        val maxSymbolsWidth = 200
        val maxSymbolsHeight = 150
        
        // CELL = 0 → Мало символов (40x30), большой шрифт
        // CELL = 100 → Много символов (180x120), маленький шрифт
        val cellPercent = params.cell / 100f
        
        // Вычисляем количество символов на основе CELL (от 40x30 до 180x120)
        val minSymbolsWidth = 40
        val minSymbolsHeight = 30
        val maxSymbolsWidthCalculated = 180
        val maxSymbolsHeightCalculated = 120
        
        val charsPerRow = (minSymbolsWidth + (maxSymbolsWidthCalculated - minSymbolsWidth) * cellPercent).toInt()
            .coerceIn(minSymbolsWidth, maxSymbolsWidth)
        val rowsCount = (minSymbolsHeight + (maxSymbolsHeightCalculated - minSymbolsHeight) * cellPercent).toInt()
            .coerceIn(minSymbolsHeight, maxSymbolsHeight)
        
        // Вычисляем оптимальный размер шрифта на основе количества символов и размера экрана
        val optimalFontSize = calculateOptimalFontSize(charsPerRow, rowsCount, screenWidth, screenHeight)
        
        // Размер ячейки для обработки изображения (фиксированный для производительности)
        val cellSize = 2 // Фиксированный размер для стабильной производительности
        
        // Создаем уменьшенную версию изображения для обработки
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, charsPerRow * cellSize, rowsCount * cellSize, true)
        val grayValues = bitmapToGrayValues(scaledBitmap)
        
        val result = when (effectType) {
            EffectType.ASCII -> convertToASCIIString(grayValues, params, charsPerRow, rowsCount)
            else -> convertToShapeString(grayValues, effectType, params, charsPerRow, rowsCount)
        }
        
        // Освобождаем память
        scaledBitmap.recycle()
        
        return Pair(result, optimalFontSize)
    }
    
    /**
     * Вычисляет оптимальный размер шрифта на основе количества символов и размера экрана
     */
    private fun calculateOptimalFontSize(charsPerRow: Int, rowsCount: Int, screenWidth: Int, screenHeight: Int): Float {
        // Более точные коэффициенты для моноширинного шрифта
        val charWidthFactor = 0.55f  // Ширина символа относительно размера шрифта
        val lineHeightFactor = 1.1f   // Высота строки относительно размера шрифта
        
        // Рассчитываем максимальный размер шрифта, при котором все символы поместятся на экране
        val maxFontSizeByWidth = screenWidth.toFloat() / charsPerRow / charWidthFactor
        val maxFontSizeByHeight = screenHeight.toFloat() / rowsCount / lineHeightFactor
        
        // Берем минимальный размер, чтобы все поместилось
        val optimalFontSize = minOf(maxFontSizeByWidth, maxFontSizeByHeight)
        
        // Более строгие ограничения для лучшего заполнения экрана (от 4sp до 20sp)
        return optimalFontSize.coerceIn(4f, 20f)
    }
    
    /**
     * Создает Bitmap с ASCII текстом
     */
    fun createASCIIBitmap(
        asciiText: String,
        backgroundColor: Int,
        textColor: Int,
        fontSize: Int = 12
    ): Bitmap {
        val lines = asciiText.split("\n")
        val maxWidth = lines.maxOfOrNull { it.length } ?: 0
        val lineHeight = fontSize + 2
        
        val bitmap = Bitmap.createBitmap(
            maxWidth * fontSize,
            lines.size * lineHeight,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
        
        val paint = Paint().apply {
            color = textColor
            textSize = fontSize.toFloat()
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
        }
        
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, 0f, (index + 1) * lineHeight.toFloat(), paint)
        }
        
        return bitmap
    }
    
    /**
     * Применяет эффекты к изображению
     */
    fun applyEffects(
        bitmap: Bitmap,
        params: EffectParams
    ): Bitmap {
        var result = bitmap
        
        // Применяем размытие если softy > 0
        if (params.softy > 0) {
            result = applyBlur(result, params.softy)
        }
        
        return result
    }
    
    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }
    
    private fun bitmapToGrayValues(bitmap: Bitmap): Array<IntArray> {
        val width = bitmap.width
        val height = bitmap.height
        val grayValues = Array(height) { IntArray(width) }
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                val gray = (Color.red(pixel) * 0.299 + Color.green(pixel) * 0.587 + Color.blue(pixel) * 0.114).toInt()
                grayValues[y][x] = gray
            }
        }
        
        return grayValues
    }
    
    private fun convertToASCIIString(grayValues: Array<IntArray>, params: EffectParams, charsPerRow: Int, rowsCount: Int): String {
        val height = grayValues.size
        val width = grayValues[0].size
        val cellSize = maxOf(1, (params.cell / 100f * 8f + 2f).toInt()) // От 2 до 10 пикселей
        
        val result = StringBuilder()
        
        // Генерируем ASCII для каждой строки
        for (row in 0 until rowsCount) {
            for (col in 0 until charsPerRow) {
                // Вычисляем позицию в исходном изображении
                val x = (col * width / charsPerRow).coerceAtMost(width - 1)
                val y = (row * height / rowsCount).coerceAtMost(height - 1)
                
                // Получаем среднее значение серого в области
                val avgGray = calculateAverageGray(grayValues, x, y, cellSize, width, height)
                val charIndex = (avgGray * (asciiChars.length - 1) / 255).toInt()
                val char = asciiChars[charIndex]
                
                // Применяем jitter (анимацию)
                val finalChar = applyJitter(char, params.jitter)
                result.append(finalChar)
            }
            result.append("\n")
        }
        
        return result.toString()
    }
    
    private fun convertToShapeString(
        grayValues: Array<IntArray>,
        effectType: EffectType,
        params: EffectParams,
        charsPerRow: Int,
        rowsCount: Int
    ): String {
        val height = grayValues.size
        val width = grayValues[0].size
        val cellSize = maxOf(1, (params.cell / 100f * 8f + 2f).toInt()) // От 2 до 10 пикселей
        
        val shapes = shapeChars[effectType] ?: asciiChars.toList().map { it.toString() }
        val result = StringBuilder()
        
        // Генерируем фигуры для каждой строки
        for (row in 0 until rowsCount) {
            for (col in 0 until charsPerRow) {
                // Вычисляем позицию в исходном изображении
                val x = (col * width / charsPerRow).coerceAtMost(width - 1)
                val y = (row * height / rowsCount).coerceAtMost(height - 1)
                
                // Получаем среднее значение серого в области
                val avgGray = calculateAverageGray(grayValues, x, y, cellSize, width, height)
                val shapeIndex = (avgGray * (shapes.size - 1) / 255).toInt()
                val shape = shapes[shapeIndex]
                
                // Применяем jitter (анимацию)
                val finalShape = applyJitterToShape(shape, params.jitter)
                result.append(finalShape)
            }
            result.append("\n")
        }
        
        return result.toString()
    }
    
    private fun calculateAverageGray(
        grayValues: Array<IntArray>,
        startX: Int,
        startY: Int,
        cellSize: Int,
        width: Int,
        height: Int
    ): Int {
        var sum = 0
        var count = 0
        
        for (y in startY until minOf(startY + cellSize, height)) {
            for (x in startX until minOf(startX + cellSize, width)) {
                sum += grayValues[y][x]
                count++
            }
        }
        
        return if (count > 0) sum / count else 0
    }
    
    private fun applyJitter(char: Char, jitter: Int): Char {
        if (jitter == 0) return char
        
        val jitterAmount = jitter / 20 // Масштабируем jitter
        val randomOffset = (Math.random() * jitterAmount * 2 - jitterAmount).toInt()
        
        // Простая реализация jitter - меняем символ на соседний
        val chars = asciiChars
        val currentIndex = chars.indexOf(char)
        if (currentIndex == -1) return char
        
        val newIndex = (currentIndex + randomOffset).coerceIn(0, chars.length - 1)
        return chars[newIndex]
    }
    
    private fun applyJitterToShape(shape: String, jitter: Int): String {
        if (jitter == 0) return shape
        
        val jitterAmount = jitter / 20
        val randomOffset = (Math.random() * jitterAmount * 2 - jitterAmount).toInt()
        
        // Для фигур просто возвращаем оригинальную фигуру с небольшими вариациями
        return shape
    }
    
    private fun applyBlur(bitmap: Bitmap, intensity: Int): Bitmap {
        // Простая реализация размытия
        val radius = intensity / 10f
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        // Здесь можно добавить более сложный алгоритм размытия
        // Пока что просто возвращаем оригинальное изображение
        return result
    }
    
    private fun applyEdgeDetection(bitmap: Bitmap, intensity: Int): Bitmap {
        // Простая реализация edge detection
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        // Здесь можно добавить алгоритм Sobel или Canny
        // Пока что просто возвращаем оригинальное изображение
        return result
    }
}
