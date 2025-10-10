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
                    // Индикатор цвета
                    ColorIndicator(
                        colorState = colorState,
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
    
    // Обновляем значения при изменении выбранного таба
    LaunchedEffect(selectedTab, colorState) {
        val currentColor = when (selectedTab) {
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
        
        // Конвертируем Color в HSV
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(currentColor.toArgb(), hsv)
        hue = hsv[0]
        saturation = hsv[1]
        value = hsv[2]
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Панель насыщенности/яркости и полоса прозрачности
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Основная панель насыщенности/яркости
            SaturationValuePanel(
                hue = hue,
                saturation = saturation,
                value = value,
                onSaturationValueChanged = { newSaturation, newValue ->
                    saturation = newSaturation
                    value = newValue
                    val newColor = Color.hsv(hue, saturation, value)
                    onColorChanged(newColor)
                },
                modifier = Modifier.weight(1f)
            )
            
            // Полоса прозрачности
            OpacitySlider(
                opacity = 1f, // Пока фиксированная прозрачность
                onOpacityChanged = { /* TODO: Реализовать прозрачность */ },
                modifier = Modifier.width(40.dp)
            )
        }
        
        // Полоса тона и кнопка назад
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Полоса тона (Hue)
            HueSlider(
                hue = hue,
                onHueChanged = { newHue ->
                    hue = newHue
                    val newColor = Color.hsv(hue, saturation, value)
                    onColorChanged(newColor)
                },
                modifier = Modifier.weight(1f)
            )
            
            // Кнопка назад
            BackButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            )
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
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.White,
                        Color.hsv(hue, 1f, 1f)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val panelWidth = with(density) { size.width.toDp().value }
                    val panelHeight = with(density) { size.height.toDp().value }
                    val newSaturation = (offset.x / panelWidth).coerceIn(0f, 1f)
                    val newValue = 1f - (offset.y / panelHeight).coerceIn(0f, 1f)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSaturationValueChanged(newSaturation, newValue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) { _, _ ->
                    // TODO: Реализовать перетаскивание
                }
            }
    ) {
        // Индикатор текущей позиции
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(
                    x = (saturation * (118 - 40)).dp,
                    y = ((1f - value) * (118 - 40)).dp
                )
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent)
                .border(
                    width = 3.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
        )
    }
}

/**
 * Полоса прозрачности
 */
@Composable
private fun OpacitySlider(
    opacity: Float,
    onOpacityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(118.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Red.copy(alpha = 1f),
                        Color.Red.copy(alpha = 0f)
                    )
                )
            )
    ) {
        // Индикатор текущей прозрачности
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (opacity * (118 - 40)).dp)
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent)
                .border(
                    width = 3.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
        )
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
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val sliderWidth = with(density) { size.width.toDp().value }
                    val newHue = (offset.x / sliderWidth * 360f).coerceIn(0f, 360f)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onHueChanged(newHue)
                }
            }
    ) {
        // Индикатор текущего тона
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (hue / 360f * 200).dp) // Фиксированная ширина для примера
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent)
                .border(
                    width = 3.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
        )
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
