package com.raux.myapplication_32.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.*
import com.raux.myapplication_32.data.models.EffectType
import com.raux.myapplication_32.data.models.EffectParams

/**
 * Новая модульная версия движка.
 *
 * Идея:
 * 1) GridPlanner — подбирает сетку (cols/rows) и размер шрифта по реальным метрикам глифов.
 * 2) Sampler — быстро считает среднюю яркость каждой ячейки через интегральное изображение.
 * 3) PaletteMapper — переводит яркости в символы (поддержка dithering и jitter).
 * 4) ASCIIEngineV2 — оркестратор: планирует, сэмплирует и отдаёт результат для UI.
 *
 * ВАЖНО: сюда передавайте размеры экрана в PX (не dp), иначе получите «пустой хвост» снизу.
 */

// -----------------------------
// Модель результата
// -----------------------------

data class Grid(
    val cols: Int,
    val rows: Int,
    val fontPx: Float,
    val charWidthPx: Float,
    val lineHeightPx: Float,
    val clamped: Boolean
)

sealed interface RenderResult {
    data class Text(val ascii: String, val grid: Grid) : RenderResult
    data class Image(val bitmap: Bitmap) : RenderResult
}

// -----------------------------
// Палитры символов (богатые градации для детализации)
// -----------------------------

object Palettes {
    // ASCII символы для темных → светлых областей ИЗОБРАЖЕНИЯ (10 градаций)
    // Темная область изображения (низкая яркость) → пробел, точка (менее заметные)
    // Светлая область изображения (высокая яркость) → @, %, # (более заметные)
    private const val ASCII_ORDERED = " .:=-+*#%@"
    
    // SQUARES: 12 градаций - полные блоки и частичные заполнения
    private val SQUARES = listOf(
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
    )
    
    // CIRCLES: 15 градаций - только простые символы без эмодзи
    private val CIRCLES = listOf(
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
    )
    
    // DIAMONDS: 16 градаций - только простые символы, хорошо красятся
    private val DIAMONDS = listOf(
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
    )
    
    // SHAPES: 13 градаций - узкая узнаваемая палитра, хорошо для лиц
    // Более предсказуемая и моноширинная чем старая
    private val SHAPES = listOf(
        " ",      // пустота
        "·",      // точка
        ":",      // двоеточие
        "-",      // минус
        "=",      // равно
        "+",      // плюс
        "*",      // звезда
        "o",      // маленькая буква о
        "O",      // большая буква О
        "0",      // ноль
        "■",      // квадрат заполненный
        "▓",      // темная текстура
        "█"       // полный блок
    )

    fun forEffect(effectType: EffectType): List<String> = when (effectType) {
        EffectType.ASCII -> ASCII_ORDERED.map { it.toString() }
        EffectType.SQUARES -> SQUARES
        EffectType.CIRCLES -> CIRCLES
        EffectType.DIAMONDS -> DIAMONDS
        EffectType.SHAPES -> SHAPES
    }
}

// -----------------------------
// GridPlanner — подбор сетки по реальным метрикам
// -----------------------------

object GridPlanner {
    /**
     * Измеряет метрики: p90 ширину (не max!) и высоту строки.
     * p90 вместо max - одна "толстая" глифа не ломает всю сетку.
     */
    private fun measureMetrics(
        palette: List<String>,
        textSizePx: Float,
        letterSpacingEm: Float = 0f
    ): Pair<Float, Float> {
        val paint = Paint().apply {
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
            textSize = textSizePx
            letterSpacing = letterSpacingEm
        }
        // p90 ширины вместо max - устойчивее к выбросам
        val widths = palette.map { paint.measureText(it) }.sorted()
        val idx = (widths.size * 0.9f).toInt().coerceIn(0, widths.lastIndex)
        val charW = widths.getOrElse(idx) { textSizePx * 0.6f }
        
        val fm = paint.fontMetrics
        val lineH = ceil((fm.descent - fm.ascent + fm.leading).toDouble()).toFloat()
        return charW to lineH
    }

    /**
     * Планирование сетки. Возвращает Grid с актуальными метриками.
     * УВАЖАЕТ таргет-сетку - не пересчитывает cols/rows в конце!
     * @param cellPercent 0f..1f
     * @param maxCells кап по производительности для текстового рендера
     */
    fun plan(
        screenWidthPx: Int,
        screenHeightPx: Int,
        cellPercent: Float,
        maxCells: Int,
        palette: List<String>,
        baseFontPx: Float,
        letterSpacingEm: Float = 0f
    ): Grid {
        // Расширенный диапазон: 12-220 колонок (было 60-200)
        // CELL=0% → крупные "плакатные" символы (12x8)
        // CELL=100% → максимум деталей (220x160)
        val targetCols = (12 + (220 - 12) * cellPercent).toInt()
        val targetRows = (8 + (160 - 8) * cellPercent).toInt()

        // Итеративная подгонка размера шрифта
        var fontPx = baseFontPx
        var colsPlan = targetCols
        var rowsPlan = targetRows
        
        repeat(2) {
            val (charW, lineH) = measureMetrics(palette, fontPx, letterSpacingEm)
            val maxCols = floor(screenWidthPx / charW).toInt().coerceAtLeast(4)
            val maxRows = floor(screenHeightPx / lineH).toInt().coerceAtLeast(4)
            
            colsPlan = min(targetCols, maxCols)
            rowsPlan = min(targetRows, maxRows)
            
            val scaleW = screenWidthPx / (colsPlan * charW)
            val scaleH = screenHeightPx / (rowsPlan * lineH)
            fontPx = (fontPx * min(scaleW, scaleH) * 0.985f).coerceIn(6f, 64f)
        }

        // Финальные метрики
        val (charW, lineH) = measureMetrics(palette, fontPx, letterSpacingEm)
        
        // ВАЖНО: берем colsPlan/rowsPlan, а НЕ пересчитываем!
        var cols = colsPlan
        var rows = rowsPlan

        // Динамический кэп: зависит от cellPercent
        val cells = cols * rows
        var clamped = false
        
        // CELL=0% → ~25% от maxCells, CELL=100% → =maxCells
        val effMaxCells = max(
            512,  // нижний порог
            (maxCells * (0.25f + 0.75f * cellPercent)).toInt()
        )
        
        if (cells > effMaxCells) {
            val k = sqrt(effMaxCells.toFloat() / cells)
            cols = max(8, floor(cols * k).toInt())
            rows = max(8, floor(rows * k).toInt())
            clamped = true
        }
        return Grid(cols, rows, fontPx, charW, lineH, clamped)
    }
}

// -----------------------------
// Sampler — интегральное изображение
// -----------------------------

object Sampler {
    private fun toGray(bitmap: Bitmap): IntArray {
        val w = bitmap.width; val h = bitmap.height
        val px = IntArray(w * h)
        bitmap.getPixels(px, 0, w, 0, 0, w, h)
        for (i in px.indices) {
            val p = px[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            px[i] = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
        }
        return px
    }

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
     * Возвращает яркости (0..255) по ячейкам сетки cols*rows.
     * @param contrast 0..100 - регулировка контраста
     * @param gamma γ-коррекция (γ>1 тянет тени, сохраняет светлые детали)
     * @param clip процент клипа по краям гистограммы для автоуровней
     */
    fun luminanceGrid(
        bitmap: Bitmap,
        cols: Int,
        rows: Int,
        contrast: Int = 0,
        gamma: Float = 1.6f,            // было 1.25 - делало полутона светлее
        clip: Float = 0.01f,            // 1% клипа по краям гистограммы
        autoLevels: Boolean = true
    ): IntArray {
        val w = bitmap.width; val h = bitmap.height
        val gray = toGray(bitmap)

        // Автоуровни: определяем черную и белую точки по гистограмме
        var black = 0; var white = 255
        if (autoLevels) {
            val hist = IntArray(256)
            for (v in gray) hist[v]++
            fun pct(p: Float): Int {
                val target = ((w * h) * p).toInt()
                var acc = 0
                for (i in 0..255) { 
                    acc += hist[i]
                    if (acc >= target) return i
                }
                return 255
            }
            black = pct(clip)
            white = pct(1f - clip)
            if (white <= black + 1) { black = 0; white = 255 }
            
            // EMA сглаживание auto-levels между кадрами (убирает мерцание)
            val smoothed = ASCIIEngineV2.smoothLevels(black, white)
            black = smoothed.first
            white = smoothed.second
        }

        val ii = buildIntegral(gray, w, h)
        val out = IntArray(cols * rows)
        val cf = 1.0f + (contrast / 100f) * 2.2f  // усиленный контраст (было 1.8)

        var idx = 0
        for (r in 0 until rows) {
            val y0 = (r * h) / rows
            val y1 = ((r + 1) * h) / rows
            for (c in 0 until cols) {
                val x0 = (c * w) / cols
                val x1 = ((c + 1) * w) / cols
                var v = rectAvg(ii, w, x0, y0, x1, y1)

                // Автоуровни - обрезаем мусорные хвосты гистограммы
                v = (((v - black) * 255f) / (white - black).coerceAtLeast(1)).toInt().coerceIn(0, 255)

                // Контраст вокруг 128
                v = (((v - 128) * cf) + 128).toInt().coerceIn(0, 255)

                // Мягкая S-кривая для лучшего распределения тонов
                val t = v / 255f
                val s = (1.1f * t - 0.05f).coerceIn(0f, 1f)
                
                // γ-коррекция (γ>1 — тянет тени, сохраняет светлые детали)
                val nv = s.toDouble().pow(1.0 / gamma).toFloat()
                out[idx++] = (nv * 255f).toInt().coerceIn(0, 255)
            }
        }
        return out
    }
}

// -----------------------------
// Post - постобработка (резкость)
// -----------------------------

object Post {
    /**
     * Unsharp mask - повышение резкости краев
     * @param amount сила эффекта (обычно 0.4-0.8)
     */
    fun unsharp(l: IntArray, cols: Int, rows: Int, amount: Float = 0.6f): IntArray {
        val blur = boxBlurGrid(l, cols, rows)
        val out = IntArray(l.size)
        for (i in l.indices) {
            val v = l[i] + ((l[i] - blur[i]) * amount).toInt()
            out[i] = v.coerceIn(0, 255)
        }
        return out
    }
    
    private fun boxBlurGrid(l: IntArray, cols: Int, rows: Int): IntArray {
        val out = IntArray(l.size)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                var s = 0; var n = 0
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        val y = r + dy; val x = c + dx
                        if (x in 0 until cols && y in 0 until rows) {
                            s += l[y * cols + x]
                            n++
                        }
                    }
                }
                out[r * cols + c] = s / n
            }
        }
        return out
    }
}

// -----------------------------
// Blur (Box Blur)
// -----------------------------

object Blur {
    fun applyBoxBlur(bitmap: Bitmap, radius: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val temp = IntArray(width * height)

        // Горизонтально
        for (y in 0 until height) {
            var rSum = 0; var gSum = 0; var bSum = 0
            val base = y * width
            for (x in 0 until width) {
                if (x == 0) {
                    rSum = 0; gSum = 0; bSum = 0
                    for (dx in -radius..radius) {
                        val nx = (dx).coerceIn(0, width - 1)
                        val p = pixels[base + nx]
                        rSum += (p shr 16) and 0xFF
                        gSum += (p shr 8) and 0xFF
                        bSum += p and 0xFF
                    }
                } else {
                    val xOut = (x - radius - 1).coerceIn(0, width - 1)
                    val xIn = (x + radius).coerceIn(0, width - 1)
                    val pOut = pixels[base + xOut]
                    val pIn = pixels[base + xIn]
                    rSum += ((pIn shr 16) and 0xFF) - ((pOut shr 16) and 0xFF)
                    gSum += ((pIn shr 8) and 0xFF) - ((pOut shr 8) and 0xFF)
                    bSum += (pIn and 0xFF) - (pOut and 0xFF)
                }
                val count = radius * 2 + 1
                temp[base + x] = Color.rgb(rSum / count, gSum / count, bSum / count)
            }
        }

        // Вертикально
        for (x in 0 until width) {
            var rSum = 0; var gSum = 0; var bSum = 0
            for (y in 0 until height) {
                val idx = y * width + x
                if (y == 0) {
                    rSum = 0; gSum = 0; bSum = 0
                    for (dy in -radius..radius) {
                        val ny = (dy).coerceIn(0, height - 1)
                        val p = temp[ny * width + x]
                        rSum += (p shr 16) and 0xFF
                        gSum += (p shr 8) and 0xFF
                        bSum += p and 0xFF
                    }
                } else {
                    val yOut = (y - radius - 1).coerceIn(0, height - 1)
                    val yIn = (y + radius).coerceIn(0, height - 1)
                    val pOut = temp[yOut * width + x]
                    val pIn = temp[yIn * width + x]
                    rSum += ((pIn shr 16) and 0xFF) - ((pOut shr 16) and 0xFF)
                    gSum += ((pIn shr 8) and 0xFF) - ((pOut shr 8) and 0xFF)
                    bSum += (pIn and 0xFF) - (pOut and 0xFF)
                }
                val count = radius * 2 + 1
                pixels[idx] = Color.rgb(rSum / count, gSum / count, bSum / count)
            }
        }

        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }
}

// -----------------------------
// PaletteMapper — из яркости в символы (с dithering и jitter)
// -----------------------------

object PaletteMapper {
    // Bayer 8x8 (0..63) - публичный для использования в гистерезисе
    val BAYER_8 = arrayOf(
        intArrayOf(0, 32, 8, 40, 2, 34, 10, 42),
        intArrayOf(48, 16, 56, 24, 50, 18, 58, 26),
        intArrayOf(12, 44, 4, 36, 14, 46, 6, 38),
        intArrayOf(60, 28, 52, 20, 62, 30, 54, 22),
        intArrayOf(3, 35, 11, 43, 1, 33, 9, 41),
        intArrayOf(51, 19, 59, 27, 49, 17, 57, 25),
        intArrayOf(15, 47, 7, 39, 13, 45, 5, 37),
        intArrayOf(63, 31, 55, 23, 61, 29, 53, 21)
    )

    // Быстрый детерминированный хеш для стабильного jitter (без мерцания)
    fun hash32(x: Int, y: Int, seed: Int): Int {
        var h = seed xor (x * 0x9E3779B9.toInt())
        h = (h xor (h ushr 16)) * 0x85EBCA6B.toInt()
        h = (h xor (h ushr 13)) * 0xC2B2AE35.toInt()
        return h xor (h ushr 16)
    }

    fun map(
        luminance: IntArray, // 0..255, size = cols*rows
        cols: Int,
        rows: Int,
        palette: List<String>,
        jitter: Int = 0,     // 0..100
        dither: Boolean = false,
        seed: Int = 1337     // seed для детерминированного jitter
    ): String {
        val n = palette.size
        val sb = StringBuilder(rows * (cols + 1))
        val jitterRange = (jitter / 30).coerceAtLeast(0) // мягкий jitter

        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val v = luminance[idx++]
                var nv = v / 255f
                if (dither) {
                    val t = (BAYER_8[r and 7][c and 7] / 63f) - 0.5f
                    nv = (nv + t * (1f / n)).coerceIn(0f, 1f)
                }
                var pi = floor(nv * (n - 1)).toInt()
                if (jitterRange > 0) {
                    // Детерминированный jitter - одна и та же клетка всегда дает одно смещение
                    val j = (hash32(c, r, seed) % (2 * jitterRange + 1)) - jitterRange
                    pi = (pi + j).coerceIn(0, n - 1)
                }
                sb.append(palette[pi])
            }
            if (r != rows - 1) sb.append('\n')
        }
        return sb.toString()
    }
}

// -----------------------------
// Основной оркестратор
// -----------------------------

object ASCIIEngineV2 {
    // Кэш сетки для стабильности (убирает "дрейф" cols/rows)
    private data class GridKey(
        val w: Int, val h: Int,
        val cellPercent: Float,
        val paletteId: Int,
        val baseFontPx: Float,
        val letterSpacingEm: Float,
        val maxCells: Int
    )
    private var cached: Pair<GridKey, Grid>? = null
    
    // Стабилизация auto-levels через EMA (убирает мерцание)
    internal data class Levels(var black: Int, var white: Int)
    internal var levelsCache: Levels? = null
    internal const val LEVELS_ALPHA = 0.2f  // 0..1, чем меньше — тем стабильнее
    
    // Гистерезис квантования (убирает мерцание на границах палитры)
    private var prevIdx: IntArray? = null
    private const val HYST = 0.06f  // ~6% от диапазона палитры
    
    private fun ensurePrev(size: Int) {
        if (prevIdx == null || prevIdx!!.size != size) prevIdx = IntArray(size) { -1 }
    }
    
    internal fun smoothLevels(black: Int, white: Int): Pair<Int, Int> {
        val prev = levelsCache
        val b = if (prev == null) black else (prev.black + ((black - prev.black) * LEVELS_ALPHA)).toInt()
        val w = if (prev == null) white else (prev.white + ((white - prev.white) * LEVELS_ALPHA)).toInt()
        levelsCache = Levels(b, w)
        return b to w
    }

    private fun planCached(
        screenWidthPx: Int,
        screenHeightPx: Int,
        cellPercent: Float,
        maxCells: Int,
        palette: List<String>,
        baseFontPx: Float,
        letterSpacingEm: Float
    ): Grid {
        val key = GridKey(
            screenWidthPx, screenHeightPx,
            cellPercent, palette.hashCode(),
            baseFontPx, letterSpacingEm, maxCells
        )
        
        // Сброс кэшей стабилизации при смене сетки/эффекта
        if (cached?.first != key) {
            prevIdx = null      // сбрасываем гистерезис
            levelsCache = null  // сбрасываем EMA auto-levels
        }
        
        cached?.let { if (it.first == key) return it.second }
        val g = GridPlanner.plan(
            screenWidthPx, screenHeightPx, cellPercent, maxCells, palette, baseFontPx, letterSpacingEm
        )
        cached = key to g
        return g
    }
    
    /**
     * Маппинг с гистерезисом - убирает мерцание на границах палитры
     */
    private fun mapWithHysteresis(
        lum: IntArray,
        cols: Int,
        rows: Int,
        palette: List<String>,
        jitter: Int,
        dither: Boolean,
        seed: Int
    ): String {
        ensurePrev(lum.size)
        val prev = prevIdx!!
        val n = palette.size
        val sb = StringBuilder(rows * (cols + 1))
        val jitterRange = (jitter / 30).coerceAtLeast(0)

        var i = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val v = lum[i]
                var nv = v / 255f
                if (dither) {
                    val t = (PaletteMapper.BAYER_8[r and 7][c and 7] / 63f) - 0.5f
                    nv = (nv + t * (1f / n)).coerceIn(0f, 1f)
                }
                var ideal = floor(nv * (n - 1)).toInt().coerceIn(0, n - 1)
                
                // Детерминированный jitter
                if (jitterRange > 0) {
                    val j = (PaletteMapper.hash32(c, r, seed) % (2 * jitterRange + 1)) - jitterRange
                    ideal = (ideal + j).coerceIn(0, n - 1)
                }

                // Гистерезис: не переключаем символ пока не уйдем от порога на HYST
                val p = prev[i]
                val chosen = if (p >= 0) {
                    val low = max(0, p - 1)
                    val high = min(n - 1, p + 1)
                    val left = (p / (n - 1f)) - HYST
                    val right = (p / (n - 1f)) + HYST
                    if (nv in left..right) p else ideal.coerceIn(low, high)
                } else ideal

                prev[i] = chosen
                sb.append(palette[chosen])
                i++
            }
            if (r != rows - 1) sb.append('\n')
        }
        return sb.toString()
    }
    
    /**
     * Рендер текста. Возвращает готовую строку и метрики сетки для корректного отображения в Compose.
     * 
     * @param params.cell - детализация (0..100)
     * @param params.jitter - детерминированное изменение символов (0..100) - БЕЗ мерцания!
     * @param params.softy - регулировка КОНТРАСТА (0..100) - 0=нет изменений, 100=максимальный контраст
     *
     * В UI:
     * Text(
     *   text = result.ascii,
     *   fontSize = with(LocalDensity.current){ result.grid.fontPx.toSp() },
     *   fontFamily = FontFamily.Monospace,
     *   softWrap = false,
     *   overflow = TextOverflow.Clip,
     *   maxLines = result.grid.rows,
     *   lineHeight = with(LocalDensity.current){ result.grid.lineHeightPx.toSp() },
     *   style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
     * )
     */
    fun renderText(
        source: Bitmap,
        effectType: EffectType,
        params: EffectParams,
        screenWidthPx: Int,
        screenHeightPx: Int,
        // UI density не нужен тут — передаём/возвращаем всё в PX
        baseFontPx: Float = 24f,
        letterSpacingEm: Float = 0f,
        maxCells: Int = 80_000,  // увеличен для большей детализации
        dither: Boolean = false,     // false для видео/потоков - убирает мерцание
        gamma: Float = 1.6f,         // оптимальное значение для детализации
        unsharpAmount: Float = 0.6f  // резкость краев
    ): RenderResult.Text {
        val cellPercent = (params.cell / 100f).coerceIn(0f, 1f)
        val palette = Palettes.forEffect(effectType)

        // Используем кэш сетки - убирает "дрейф" cols/rows
        val grid = planCached(
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            cellPercent = cellPercent,
            maxCells = maxCells,
            palette = palette,
            baseFontPx = baseFontPx,
            letterSpacingEm = letterSpacingEm
        )

        // Рассчитываем яркости с автоуровнями и улучшенной gamma
        var lum = Sampler.luminanceGrid(
            bitmap = source,
            cols = grid.cols,
            rows = grid.rows,
            contrast = params.softy,  // SOFTY регулирует контраст!
            gamma = gamma,
            clip = 0.01f,
            autoLevels = true
        )
        
        // Применяем unsharp mask для резкости краев (добавляет детали!)
        lum = Post.unsharp(lum, grid.cols, grid.rows, amount = unsharpAmount)

        // Мапим в символы с ГИСТЕРЕЗИСОМ - убирает мерцание на границах палитры!
        val ascii = mapWithHysteresis(
            lum = lum,
            cols = grid.cols,
            rows = grid.rows,
            palette = palette,
            jitter = params.jitter,
            dither = dither,
            seed = 1337
        )

        // Всегда возвращаем Text (для совместимости с существующим UI)
        return RenderResult.Text(ascii, grid)
    }
    
    /**
     * Рендер по фиксированной сетке ячеек - гарантирует полное заполнение экрана.
     * Для не-ASCII эффектов (SHAPES/CIRCLES/DIAMONDS/SQUARES) - убирает "дыры".
     */
    private fun renderFixedGridBitmap(
        ascii: String,
        grid: Grid,
        screenW: Int,
        screenH: Int,
        backgroundColor: Int = Color.BLACK,
        textColor: Int = Color.WHITE
    ): RenderResult.Image {
        val bmp = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(backgroundColor)

        // Точные размеры клеток - заполняют весь экран
        val cw = screenW.toFloat() / grid.cols
        val ch = screenH.toFloat() / grid.rows

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = android.graphics.Typeface.MONOSPACE
            color = textColor
            textSize = min(cw, ch) * 0.95f  // подгон под ячейку
            letterSpacing = 0f
        }
        val fm = paint.fontMetrics
        val baselineShift = -fm.ascent.coerceAtLeast(ch * 0.8f)

        var r = 0
        var c = 0
        ascii.forEach { chx ->
            if (chx == '\n') {
                r++
                c = 0
                return@forEach
            }
            val x = c * cw
            val y = r * ch + baselineShift
            canvas.drawText(chx.toString(), x, y, paint)
            c++
        }
        return RenderResult.Image(bmp)
    }

    /**
     * Пререндер в Bitmap (когда текст слишком тяжёлый или нужен стабильный fill).
     * Быстрее будет через атлас глифов, но для простоты — рисуем построчно.
     */
    fun renderBitmap(
        text: String,
        grid: Grid,
        backgroundColor: Int = Color.BLACK,
        textColor: Int = Color.WHITE
    ): RenderResult.Image {
        val paint = Paint().apply {
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
            color = textColor
            textSize = grid.fontPx
            letterSpacing = 0f
        }
        val fm = paint.fontMetrics
        val lineH = grid.lineHeightPx
        val colsW = (grid.cols * grid.charWidthPx).roundToInt()
        val rowsH = (grid.rows * lineH).roundToInt()

        val bmp = Bitmap.createBitmap(colsW, rowsH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(backgroundColor)

        var y = -fm.ascent
        val lines = text.split('\n')
        for (line in lines) {
            canvas.drawText(line, 0f, y, paint)
            y += lineH
        }
        return RenderResult.Image(bmp)
    }
}

