package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raux.myapplication_32.data.models.ColorState
import com.raux.myapplication_32.data.models.SymbolPaint
import com.raux.myapplication_32.ui.theme.*

/**
 * Блок выбора цвета согласно дизайну Figma
 * Содержит табы с индикаторами цвета и HSV пикер
 */
@Composable
fun ColorPickerBlock(
    colorState: ColorState,
    selectedTab: ColorTab,
    onTabSelected: (ColorTab) -> Unit,
    onBackgroundColorChanged: (Color) -> Unit,
    onSymbolColorChanged: (Color) -> Unit,
    onGradientChanged: (Color, Color) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(266.dp) // Фиксированная высота как в дизайне
            .background(Color.Black)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Табы выбора типа цвета
        ColorTabs(
            colorState = colorState,
            selectedTab = selectedTab,
            onTabSelected = { tab ->
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onTabSelected(tab)
            }
        )
        
        // HSV цветовой пикер
        HSVColorPicker(
            colorState = colorState,
            selectedTab = selectedTab,
            onColorChanged = { color ->
                when (selectedTab) {
                    ColorTab.BACKGROUND -> onBackgroundColorChanged(color)
                    ColorTab.COLOR1 -> onSymbolColorChanged(color)
                    ColorTab.GRADIENT -> {
                        val currentSymbols = colorState.symbols
                        if (currentSymbols is SymbolPaint.Gradient) {
                            onGradientChanged(color, currentSymbols.end)
                        } else {
                            onGradientChanged(color, color)
                        }
                    }
                }
            },
            onBackClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onBackClick()
            }
        )
    }
}

/**
 * Табы для выбора типа цвета
 */
@Composable
private fun ColorTabs(
    colorState: ColorState,
    selectedTab: ColorTab,
    onTabSelected: (ColorTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ColorTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            
            // Получаем актуальный цвет для каждого таба
            val tabColor = when (tab) {
                ColorTab.BACKGROUND -> colorState.background
                ColorTab.COLOR1 -> {
                    when (val symbols = colorState.symbols) {
                        is SymbolPaint.Solid -> symbols.color
                        is SymbolPaint.Gradient -> symbols.start
                    }
                }
                ColorTab.GRADIENT -> {
                    when (val symbols = colorState.symbols) {
                        is SymbolPaint.Solid -> symbols.color
                        is SymbolPaint.Gradient -> symbols.start
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) AppColors.GreyActive else AppColors.MainGrey
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Индикатор цвета с актуальным цветом
                    ColorIndicator(
                        colorState = ColorState(
                            background = tabColor,
                            symbols = when (tab) {
                                ColorTab.BACKGROUND -> SymbolPaint.Solid(tabColor)
                                ColorTab.COLOR1 -> SymbolPaint.Solid(tabColor)
                                ColorTab.GRADIENT -> colorState.symbols
                            }
                        ),
                        isSelected = isSelected,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    // Текст таба
                    Text(
                        text = tab.displayName,
                        style = AppTypography.body1.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isSelected) AppColors.White else AppColors.White.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * HSV цветовой пикер
 */
@Composable
private fun HSVColorPicker(
    colorState: ColorState,
    selectedTab: ColorTab,
    onColorChanged: (Color) -> Unit,
    onBackClick: () -> Unit
) {
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(1f) }
    var alpha by remember { mutableStateOf(1f) }

    // ⬇️ перезапускаем ТОЛЬКО при смене таба
    LaunchedEffect(selectedTab) {
        val current = when (selectedTab) {
            ColorTab.BACKGROUND -> colorState.background
            ColorTab.COLOR1, ColorTab.GRADIENT -> when (val s = colorState.symbols) {
                is SymbolPaint.Solid -> s.color
                is SymbolPaint.Gradient -> s.start
            }
        }
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(current.toArgb(), hsv)
        hue = hsv[0]; saturation = hsv[1]; value = hsv[2]; alpha = current.alpha
    }

    // ⬇️ стабилизируем ссылку на коллбэк (чтобы pointerInput был с ключом Unit)
    val applyColor by rememberUpdatedState(onColorChanged)

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SaturationValuePanel(
                hue = hue,
                saturation = saturation,
                value = value,
                onSaturationValueChanged = { s, v ->
                    saturation = s; value = v
                    applyColor(Color.hsv(hue, saturation, value, alpha))
                },
                modifier = Modifier.weight(1f)
            )
            OpacitySlider(
                baseColor = Color.hsv(hue, saturation, value),
                opacity = alpha,
                onOpacityChanged = { a ->
                    alpha = a
                    applyColor(Color.hsv(hue, saturation, value, alpha))
                },
                modifier = Modifier.width(40.dp)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HueSlider(
                hue = hue,
                onHueChanged = { h ->
                    hue = h
                    applyColor(Color.hsv(hue, saturation, value, alpha))
                },
                modifier = Modifier.weight(1f)
            )
            BackButton(onClick = onBackClick, modifier = Modifier.size(40.dp))
        }
    }
}

/**
 * Панель насыщенности и яркости
 */
@Composable
private fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationValueChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val onSV by rememberUpdatedState(onSaturationValueChanged)
    var sizePx by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    fun compute(p: Offset): Pair<Float, Float>? {
        if (sizePx.width == 0 || sizePx.height == 0) return null
        val s = (p.x / sizePx.width).coerceIn(0f, 1f)
        val v = (1f - p.y / sizePx.height).coerceIn(0f, 1f)
        return s to v
    }

    Box(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue, 1f, 1f))))
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            .onGloballyPositioned { sizePx = it.size }
            // ⬇️ стабильный ключ
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    compute(p)?.let { (s, v) ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSV(s, v)
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) { change, _ ->
                    change.consume() // ⬅️ предотвращаем конкуренцию жестов
                    compute(change.position)?.let { (s, v) -> onSV(s, v) }
                }
            }
    ) {
        // индикатор — без изменений
        if (sizePx.width > 0 && sizePx.height > 0) {
            val handle = 40.dp
            val handlePx = with(LocalDensity.current) { handle.toPx() }
            val x = (saturation * sizePx.width - handlePx / 2).coerceIn(0f, sizePx.width - handlePx)
            val y = ((1f - value) * sizePx.height - handlePx / 2).coerceIn(0f, sizePx.height - handlePx)
            Box(
                Modifier
                    .offset { androidx.compose.ui.unit.IntOffset(x.toInt(), y.toInt()) }
                    .size(handle)
                    .clip(RoundedCornerShape(12.dp))
                    .border(3.dp, Color.White, RoundedCornerShape(12.dp))
            )
        }
    }
}

/**
 * Полоса прозрачности
 */
@Composable
private fun OpacitySlider(
    baseColor: Color,
    opacity: Float,
    onOpacityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val onAlpha by rememberUpdatedState(onOpacityChanged)
    val haptic = LocalHapticFeedback.current
    var sizePx by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    fun compute(p: Offset): Float? {
        if (sizePx.height == 0) return null
        return (1f - p.y / sizePx.height).coerceIn(0f, 1f)
    }

    Box(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(baseColor.copy(alpha = 1f), baseColor.copy(alpha = 0f))))
            .onGloballyPositioned { sizePx = it.size }
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    compute(p)?.let {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAlpha(it)
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                ) { change, _ ->
                    change.consume()
                    compute(change.position)?.let { onAlpha(it) }
                }
            }
    ) {
        if (sizePx.height > 0) {
            val handle = 40.dp
            val handlePx = with(LocalDensity.current) { handle.toPx() }
            val y = ((1f - opacity) * sizePx.height - handlePx / 2).coerceIn(0f, sizePx.height - handlePx)
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .offset { androidx.compose.ui.unit.IntOffset(0, y.toInt()) }
                    .size(handle)
                    .clip(RoundedCornerShape(12.dp))
                    .border(3.dp, Color.White, RoundedCornerShape(12.dp))
            )
        }
    }
}

/**
 * Полоса тона (Hue)
 */
@Composable
private fun HueSlider(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val onHue by rememberUpdatedState(onHueChanged)
    var sizePx by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    fun compute(p: Offset): Float? {
        if (sizePx.width == 0) return null
        return (p.x / sizePx.width * 360f).coerceIn(0f, 360f)
    }

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                )
            )
            .onGloballyPositioned { sizePx = it.size }
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    compute(p)?.let {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onHue(it)
                    }
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                ) { change, _ ->
                    change.consume()
                    compute(change.position)?.let { onHue(it) }
                }
            }
    ) {
        if (sizePx.width > 0) {
            val handle = 40.dp
            val handlePx = with(LocalDensity.current) { handle.toPx() }
            val x = (hue / 360f * sizePx.width - handlePx / 2).coerceIn(0f, sizePx.width - handlePx)
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset { androidx.compose.ui.unit.IntOffset(x.toInt(), 0) }
                    .size(handle)
                    .clip(RoundedCornerShape(12.dp))
                    .border(3.dp, Color.White, RoundedCornerShape(12.dp))
            )
        }
    }
}

/**
 * Кнопка назад
 */
@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Назад",
            tint = AppColors.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Типы табов для выбора цвета
 */
enum class ColorTab(val displayName: String) {
    BACKGROUND("bg Color"),
    COLOR1("color #1"),
    GRADIENT("gradient")
}
