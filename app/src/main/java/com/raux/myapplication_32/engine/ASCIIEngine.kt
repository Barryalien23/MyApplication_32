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
 * v4.0 - С реальными метриками глифов
 */
class ASCIIEngine {
    
    // Helper функция для форматирования Float
    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
    
    // Data class для хранения реальных метрик глифов
    private data class GlyphMetrics(val charWidthPx: Float, val lineHeightPx: Float)
    
    // Максимальное количество ячеек для производительности
    private val MAX_CELLS = 18_000
    
        // ASCII символы от самых темных к самым светлым (улучшенный набор для плотности)
        private val asciiChars = " .'`^\",:;Il!i><~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$"
    
    // Улучшенные символы для разных эффектов (больше градаций для лучшей детализации)
    private val shapeChars = mapOf(
        EffectType.ASCII to asciiChars.toList().map { it.toString() },
        
        // SQUARES: 17 градаций (было 9) - полные блоки и частичные заполнения
        EffectType.SQUARES to listOf(
            " ",      // 0% заполнения
            "░",      // 25% заполнения (светлая текстура)
            "▁",      // нижний 1/8
            "▂",      // нижний 2/8
            "▃",      // нижний 3/8
            "▄",      // нижний 4/8 (половина)
            "▒",      // 50% заполнения (средняя текстура)
            "▅",      // нижний 5/8
            "▆",      // нижний 6/8
            "▇",      // нижний 7/8
            "▓",      // 75% заполнения (темная текстура)
            "█"       // 100% заполнения (полный блок)
        ),
        
        // CIRCLES: 15 градаций - только простые символы без эмодзи
        EffectType.CIRCLES to listOf(
            " ",      // пустота
            ".",      // мини точка
            "·",      // маленькая точка
            "˙",      // точка выше
            "∙",      // математическая точка
            "⋅",      // точка оператор
            "∘",      // круг композиция
            "○",      // пустой круг маленький
            "◌",      // пунктирный круг
            "◦",      // белый круг
            "⊙",      // круг с точкой в центре
            "⊚",      // круг с точкой внутри
            "●",      // заполненный круг
            "◉",      // круг с точкой
            "◎"       // двойной круг
        ),
        
        // DIAMONDS: 16 градаций - только простые символы, хорошо красятся
        EffectType.DIAMONDS to listOf(
            " ",      // пустота
            ".",      // точка
            "·",      // средняя точка
            "▖",      // левый нижний квадрант
            "▗",      // правый нижний квадрант
            "▘",      // левый верхний квадрант
            "▝",      // правый верхний квадрант
            "▚",      // диагональ 1
            "▞",      // диагональ 2
            "◇",      // белый ромб
            "◈",      // белый ромб с X
            "▙",      // три квадранта (без правого верхнего)
            "▛",      // три квадранта (без правого нижнего)
            "▜",      // три квадранта (без левого нижнего)
            "▟",      // три квадранта (без левого верхнего)
            "█"       // полный блок
        ),
        
        // SHAPES: 20 градаций - микс различных символов, хорошо красятся
        EffectType.SHAPES to listOf(
            " ",      // пустота
            ".",      // точка
            "·",      // средняя точка
            "°",      // градус
            "˚",      // кольцо
            "∘",      // композиция
            "▪",      // маленький квадрат
            "▫",      // маленький белый квадрат
            "□",      // белый квадрат
            "▢",      // белый квадрат с округлыми углами
            "▣",      // белый квадрат с горизонтальной линией
            "▤",      // квадрат с горизонтальным заполнением
            "▥",      // квадрат с вертикальным заполнением
            "▦",      // квадрат с ортогональным заполнением
            "▧",      // квадрат с диагональным заполнением
            "▨",      // квадрат с диагональным заполнением
            "▩",      // квадрат с крестовым заполнением
            "■",      // квадрат заполненный
            "▬",      // прямоугольник
            "█"       // полный блок
        )
    )
    
    // Счетчик для демонстрации изменений (отключен для производительности)
    // private var frameCounter = 0
    
    /**
     * Измеряет реальные метрики глифов с учетом текущего шрифта
     */
    private fun measureMetrics(
        effectType: EffectType,
        textSizePx: Float,
        letterSpacingEm: Float = 0f
    ): GlyphMetrics {
        val paint = Paint().apply {
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
            textSize = textSizePx
            letterSpacing = letterSpacingEm
        }
        
        val glyphs = when (effectType) {
            EffectType.ASCII -> asciiChars.map { it.toString() }
            else -> (shapeChars[effectType] ?: asciiChars.map { it.toString() })
        }
        
        // Берём ширину самого широкого глифа (безопасно от переноса)
        var maxW = 0f
        for (g in glyphs) {
            maxW = max(maxW, paint.measureText(g))
        }
        
        val fm = paint.fontMetrics
        val lineH = ceil((fm.descent - fm.ascent + fm.leading).toDouble()).toFloat()
        return GlyphMetrics(maxW, lineH)
    }
    
    /**
     * Конвертация sp в px
     */
    private fun spToPx(sp: Float, density: Float) = sp * density
    
    /**
     * Линейная интерполяция
     */
    private fun lerp(i0: Int, i1: Int, t: Float) = (i0 + (i1 - i0) * t).toInt()
    
    /**
     * Конвертация Bitmap в grayscale array
     */
    private fun toGray(bm: Bitmap): IntArray {
        val w = bm.width
        val h = bm.height
        val px = IntArray(w * h)
        bm.getPixels(px, 0, w, 0, 0, w, h)
        for (i in px.indices) {
            val p = px[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            px[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        }
        return px
    }
    
    /**
     * Построение интегрального изображения для быстрого вычисления средних
     */
    private fun buildIntegral(gray: IntArray, w: Int, h: Int): LongArray {
        val out = LongArray((w + 1) * (h + 1))
        var idx = 0
        for (y in 1..h) {
            var row = 0L
            for (x in 1..w) {
                row += gray[idx++]
                out[y * (w + 1) + x] = out[(y - 1) * (w + 1) + x] + row
            }
        }
        return out
    }
    
    /**
     * Вычисление среднего значения в прямоугольной области через интегральное изображение
     */
    private fun rectAvg(ii: LongArray, w: Int, x0: Int, y0: Int, x1: Int, y1: Int): Int {
        val A = ii[y0 * (w + 1) + x0]
        val B = ii[y0 * (w + 1) + x1]
        val C = ii[y1 * (w + 1) + x0]
        val D = ii[y1 * (w + 1) + x1]
        val sum = D - B - C + A
        val area = max(1, (x1 - x0) * (y1 - y0))
        return (sum / area).toInt().coerceIn(0, 255)
    }
    
    /**
     * Конвертирует Bitmap в ASCII строку с адаптивным размером
     * CELL параметр управляет количеством символов и размером шрифта
     * v4.0 - С реальными метриками глифов
     */
    fun convertToASCII(
        bitmap: Bitmap,
        effectType: EffectType,
        params: EffectParams,
        screenWidthPx: Int = 300,      // ← теперь PX, не dp
        screenHeightPx: Int = 200,     // ← теперь PX, не dp
        density: Float = 2.75f         // ← density для конвертации sp в px
    ): Pair<String, Float> {
        // ======================================================================
        // v4.1 - ИСПРАВЛЕНО: CELL параметр теперь реально управляет детализацией
        // ======================================================================
        
        val cellPercent = params.cell / 100f
        val letterSpacingEm = 0f  // Не используем отрицательный кернинг
        
        // CELL параметр управляет желаемым количеством символов
        // Диапазоны увеличены для лучшей детализации:
        // - CELL 0%: минимум символов (30x20) для быстродействия
        // - CELL 100%: максимум символов (200x140) для детализации
        val wantedCols = lerp(30, 200, cellPercent)
        val wantedRows = lerp(20, 140, cellPercent)
        
        // Ограничиваем по MAX_CELLS для производительности
        var targetCols = wantedCols
        var targetRows = wantedRows
        val wantedCells = targetCols * targetRows
        if (wantedCells > MAX_CELLS) {
            val k = sqrt(MAX_CELLS.toFloat() / wantedCells)
            targetCols = max(20, floor(targetCols * k).toInt())
            targetRows = max(15, floor(targetRows * k).toInt())
        }
        
        // Итеративно подбираем fontSize, чтобы сетка идеально заполнила экран
        var fontSp = 12f
        repeat(3) { // три итерации для высокой точности
            val metrics = measureMetrics(effectType, spToPx(fontSp, density), letterSpacingEm)
            
            // Рассчитываем необходимый scale для заполнения экрана
            val scaleW = screenWidthPx / (targetCols * metrics.charWidthPx)
            val scaleH = screenHeightPx / (targetRows * metrics.lineHeightPx)
            
            // Используем минимальный scale, чтобы все поместилось + запас 1.5% для безопасности
            val safeScale = min(scaleW, scaleH) * 0.985f
            
            fontSp = (fontSp * safeScale).coerceIn(4f, 20f)
        }
        
        // Финальный расчет с подобранным fontSize
        val finalMetrics = measureMetrics(effectType, spToPx(fontSp, density), letterSpacingEm)
        
        // Используем ТОЧНО те значения, под которые подбирали fontSize
        var charsPerRow = targetCols
        var rowsCount = targetRows
        
        // Дополнительная проверка: если всё-таки не помещается, корректируем
        val actualWidth = charsPerRow * finalMetrics.charWidthPx
        val actualHeight = rowsCount * finalMetrics.lineHeightPx
        
        if (actualWidth > screenWidthPx) {
            charsPerRow = floor(screenWidthPx / finalMetrics.charWidthPx).toInt().coerceAtLeast(4)
        }
        if (actualHeight > screenHeightPx) {
            rowsCount = floor(screenHeightPx / finalMetrics.lineHeightPx).toInt().coerceAtLeast(4)
        }
        
        val finalCells = charsPerRow * rowsCount
        
        // Логирование
        android.util.Log.d("ASCIIEngine", """
            ═══════════════════════════════════════
            ADAPTIVE ASCII ENGINE v4.1 (CELL Fix)
            ═══════════════════════════════════════
            INPUT:
            - CELL: ${params.cell}% (cellPercent: $cellPercent)
            - Screen: ${screenWidthPx}x${screenHeightPx}px (density: $density)
            
            WANTED (from CELL):
            - Target grid: ${wantedCols}x${wantedRows} = ${wantedCells} cells
            - After MAX_CELLS limit: ${targetCols}x${targetRows}
            
            METRICS (Real):
            - charWidth: ${finalMetrics.charWidthPx.format(2)}px
            - lineHeight: ${finalMetrics.lineHeightPx.format(2)}px
            
            FINAL:
            - Grid: ${charsPerRow}x${rowsCount} chars (${finalCells} total)
            - fontSize: ${fontSp.format(2)}sp
            - MAX_CELLS: ${if (wantedCells > MAX_CELLS) "LIMITED ($wantedCells → ${finalCells})" else "OK"}
            - Real width: ${(charsPerRow * finalMetrics.charWidthPx).toInt()}px / ${screenWidthPx}px (${((charsPerRow * finalMetrics.charWidthPx / screenWidthPx) * 100).toInt()}% fill)
            - Real height: ${(rowsCount * finalMetrics.lineHeightPx).toInt()}px / ${screenHeightPx}px (${((rowsCount * finalMetrics.lineHeightPx / screenHeightPx) * 100).toInt()}% fill)
            ═══════════════════════════════════════
        """.trimIndent())
        
        // Предпросчет блюра
        val processed = if (params.softy > 0) applyBlur(bitmap, params.softy) else bitmap
        
        // Быстрые средние через интегральное изображение (без createScaledBitmap!)
        val gray = toGray(processed)
        val w = processed.width
        val h = processed.height
        val ii = buildIntegral(gray, w, h)
        
        // Генерируем ASCII
        val sb = StringBuilder(rowsCount * (charsPerRow + 1))
        when (effectType) {
            EffectType.ASCII -> {
                for (r in 0 until rowsCount) {
                    val y0 = (r * h) / rowsCount
                    val y1 = ((r + 1) * h) / rowsCount
                    for (c in 0 until charsPerRow) {
                        val x0 = (c * w) / charsPerRow
                        val x1 = ((c + 1) * w) / charsPerRow
                        val avg = rectAvg(ii, w, x0, y0, x1, y1)
                        val idx = (avg * (asciiChars.length - 1) / 255f).toInt()
                        sb.append(applyJitter(asciiChars[idx], params.jitter))
                    }
                    sb.append('\n')
                }
            }
            else -> {
                val shapes = (shapeChars[effectType] ?: asciiChars.map { it.toString() })
                for (r in 0 until rowsCount) {
                    val y0 = (r * h) / rowsCount
                    val y1 = ((r + 1) * h) / rowsCount
                    for (c in 0 until charsPerRow) {
                        val x0 = (c * w) / charsPerRow
                        val x1 = ((c + 1) * w) / charsPerRow
                        val avg = rectAvg(ii, w, x0, y0, x1, y1)
                        val idx = (avg * (shapes.size - 1) / 255f).toInt()
                        sb.append(applyJitterToShape(shapes[idx], params.jitter, effectType))
                    }
                    sb.append('\n')
                }
            }
        }
        
        if (processed !== bitmap) processed.recycle()
        
        return Pair(sb.toString(), fontSp)
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
    
    private fun convertToASCIIString(
        grayValues: Array<IntArray>,
        params: EffectParams,
        charsPerRow: Int,
        rowsCount: Int,
        cellSize: Int,
        effectType: EffectType
    ): String {
        val height = grayValues.size
        val width = grayValues[0].size
        
        val result = StringBuilder()
        
        // Генерируем ASCII для каждой строки
        for (row in 0 until rowsCount) {
            for (col in 0 until charsPerRow) {
                // Вычисляем позицию в исходном изображении
                val x = (col * width / charsPerRow).coerceAtMost(width - 1)
                val y = (row * height / rowsCount).coerceAtMost(height - 1)
                
                // Получаем среднее значение серого в области с улучшенным алгоритмом
                val avgGray = calculateWeightedAverageGray(grayValues, x, y, cellSize, width, height)
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
        rowsCount: Int,
        cellSize: Int
    ): String {
        val height = grayValues.size
        val width = grayValues[0].size
        
        val shapes = shapeChars[effectType] ?: asciiChars.toList().map { it.toString() }
        val result = StringBuilder()
        
        // Генерируем фигуры для каждой строки
        for (row in 0 until rowsCount) {
            for (col in 0 until charsPerRow) {
                // Вычисляем позицию в исходном изображении
                val x = (col * width / charsPerRow).coerceAtMost(width - 1)
                val y = (row * height / rowsCount).coerceAtMost(height - 1)
                
                // Получаем среднее значение серого в области с улучшенным алгоритмом
                val avgGray = calculateWeightedAverageGray(grayValues, x, y, cellSize, width, height)
                val shapeIndex = (avgGray * (shapes.size - 1) / 255).toInt()
                val shape = shapes[shapeIndex]
                
                // Применяем jitter (анимацию)
                val finalShape = applyJitterToShape(shape, params.jitter, effectType)
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
    
    /**
     * Улучшенный алгоритм усреднения с весовыми коэффициентами
     * Центральные пиксели имеют больший вес для лучшей детализации
     */
    private fun calculateWeightedAverageGray(
        grayValues: Array<IntArray>,
        startX: Int,
        startY: Int,
        cellSize: Int,
        width: Int,
        height: Int
    ): Int {
        var weightedSum = 0f
        var totalWeight = 0f
        
        val centerX = startX + cellSize / 2f
        val centerY = startY + cellSize / 2f
        
        for (y in startY until minOf(startY + cellSize, height)) {
            for (x in startX until minOf(startX + cellSize, width)) {
                // Вычисляем вес на основе расстояния от центра
                val dx = x - centerX
                val dy = y - centerY
                val distance = sqrt(dx * dx + dy * dy)
                
                // Гауссово взвешивание: чем ближе к центру, тем больше вес
                val weight = exp(-distance * distance / (cellSize * cellSize / 2f))
                
                weightedSum += grayValues[y][x] * weight
                totalWeight += weight
            }
        }
        
        return if (totalWeight > 0) (weightedSum / totalWeight).toInt() else 0
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
    
    private fun applyJitterToShape(shape: String, jitter: Int, effectType: EffectType): String {
        if (jitter == 0) return shape
        
        val jitterAmount = jitter / 20
        val randomOffset = (Math.random() * jitterAmount * 2 - jitterAmount).toInt()
        
        // Получаем набор символов для данного эффекта
        val shapes = shapeChars[effectType] ?: return shape
        val currentIndex = shapes.indexOf(shape)
        if (currentIndex == -1) return shape
        
        // Применяем случайное смещение с учетом границ
        val newIndex = (currentIndex + randomOffset).coerceIn(0, shapes.size - 1)
        return shapes[newIndex]
    }
    
    /**
     * Применяет размытие к изображению с использованием Box Blur
     * intensity: 0-100 (конвертируется в радиус 0-10)
     */
    private fun applyBlur(bitmap: Bitmap, intensity: Int): Bitmap {
        if (intensity == 0) return bitmap
        
        // Конвертируем интенсивность 0-100 в радиус 1-10
        val radius = maxOf(1, (intensity / 10f).toInt())
        
        return applyBoxBlur(bitmap, radius)
    }
    
    /**
     * Box Blur алгоритм - быстрое и эффективное размытие
     */
    private fun applyBoxBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        // Горизонтальный проход
        val tempPixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = 0
                var g = 0
                var b = 0
                var count = 0
                
                for (dx in -radius..radius) {
                    val nx = (x + dx).coerceIn(0, width - 1)
                    val pixel = pixels[y * width + nx]
                    
                    r += Color.red(pixel)
                    g += Color.green(pixel)
                    b += Color.blue(pixel)
                    count++
                }
                
                tempPixels[y * width + x] = Color.rgb(r / count, g / count, b / count)
            }
        }
        
        // Вертикальный проход
        for (x in 0 until width) {
            for (y in 0 until height) {
                var r = 0
                var g = 0
                var b = 0
                var count = 0
                
                for (dy in -radius..radius) {
                    val ny = (y + dy).coerceIn(0, height - 1)
                    val pixel = tempPixels[ny * width + x]
                    
                    r += Color.red(pixel)
                    g += Color.green(pixel)
                    b += Color.blue(pixel)
                    count++
                }
                
                pixels[y * width + x] = Color.rgb(r / count, g / count, b / count)
            }
        }
        
        result.setPixels(pixels, 0, width, 0, 0, width, height)
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
