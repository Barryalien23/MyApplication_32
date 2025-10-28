package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.raux.myapplication_32.ui.theme.*

/**
 * Кнопка захвата фото (большая белая кнопка)
 * Дизайн согласно Figma: белая обводка с отступом, белый фон внутри, эффект сжатия при нажатии
 */
@Composable
fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCapturing: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current
    
    // Анимация отступа при нажатии (4dp -> 6dp для эффекта сжатия)
    val padding by animateFloatAsState(
        targetValue = if (isPressed) 6f else 4f,
        animationSpec = tween(
            durationMillis = 100, // Очень быстрая анимация
            easing = FastOutSlowInEasing
        ),
        label = "padding"
    )
    
    Box(
        modifier = modifier
            .width(120.dp)
            .height(60.dp)
            .shadow(
                elevation = 8.dp, // Тень согласно Figma: 0px_0px_8px_0px_rgba(0,0,0,0.08)
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp)) // Радиус 20dp согласно Figma
            .border(
                width = 2.dp, // Белая обводка 2dp
                color = Color(0x66FFFFFF), // rgba(255,255,255,0.4) - белая с прозрачностью 40%
                shape = RoundedCornerShape(20.dp)
            )
            .background(Color(0x59000000)) // Черный фон с прозрачностью 35%
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    radius = 40.dp,
                    color = Color.White.copy(alpha = 0.3f)
                ),
                enabled = !isCapturing
            ) { 
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick() 
            },
        contentAlignment = Alignment.Center
    ) {
        // Белый фон внутри с анимированным отступом
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding.dp) // Анимированный отступ: 4dp -> 6dp
                .clip(RoundedCornerShape(16.dp)) // Радиус 16dp внутри
                .background(AppColors.White) // Белый фон
        )
    }
}

/**
 * Функциональная кнопка (переключение камеры, загрузка и т.д.)
 * Дизайн согласно Figma: полупрозрачный фон с размытием и тенью
 */
@Composable
fun FunctionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    
    Box(
        modifier = modifier
            .size(52.dp) // Размер согласно Figma
            .shadow(
                elevation = 8.dp, // Тень согласно Figma: 0px_0px_8px_0px_rgba(0,0,0,0.08)
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(16.dp)) // Радиус 16dp согласно Figma
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    radius = 26.dp,
                    color = Color.White.copy(alpha = 0.3f)
                ),
                enabled = enabled
            ) { 
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick() 
            },
        contentAlignment = Alignment.Center
    ) {
        // Черная подложка с прозрачностью 35%
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x59000000)) // Черный фон с прозрачностью 35%
        )
        
        // Четкая иконка поверх размытой подложки
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) AppColors.White else AppColors.White40,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Кнопка выбора эффекта
 */
@Composable
fun EffectButton(
    effectName: String,
    icon: ImageVector,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) AppColors.White20 else AppColors.MainGrey
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = effectName,
                tint = AppColors.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = effectName,
                style = AppTypography.body1,
                color = AppColors.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Кнопка настройки параметра эффекта
 */
@Composable
fun SettingButton(
    paramName: String,
    icon: ImageVector,
    value: Int, // 0..100
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .clickable { onClick() }
    ) {
        // Прогресс-индикатор (если значение > 0)
        if (value > 0) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(value / 100f)
                    .background(
                        AppColors.White20,
                        RoundedCornerShape(12.dp)
                    )
            )
        }
        
        // Содержимое кнопки
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = paramName,
                tint = AppColors.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = paramName,
                style = AppTypography.body1,
                color = AppColors.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Кнопка выбора цвета
 */
@Composable
fun ColorButton(
    colorName: String,
    selectedColor: Color?,
    isGradient: Boolean = false,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .clickable(enabled = isEnabled) { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Иконка цвета
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = if (isEnabled) AppColors.White else AppColors.White20,
                        shape = CircleShape
                    )
                    .background(
                        selectedColor ?: AppColors.White,
                        CircleShape
                    )
            )
            
            Text(
                text = colorName,
                style = AppTypography.body1,
                color = if (isEnabled) AppColors.White else AppColors.White40,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Кнопка сохранения обработанного изображения
 * Дизайн идентичен CaptureButton - белая кнопка с черным текстом "SAVE IMAGE"
 */
@Composable
fun SaveImageButton(
    onClick: () -> Unit,
    isProcessing: Boolean = false,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val hapticFeedback = LocalHapticFeedback.current
    
    // Анимация отступа при нажатии (4dp -> 6dp для эффекта сжатия)
    val padding by animateFloatAsState(
        targetValue = if (isPressed) 6f else 4f,
        animationSpec = tween(
            durationMillis = 100, // Очень быстрая анимация
            easing = FastOutSlowInEasing
        ),
        label = "padding"
    )
    
    Box(
        modifier = modifier
            .width(120.dp)
            .height(60.dp)
            .shadow(
                elevation = 8.dp, // Тень согласно Figma: 0px_0px_8px_0px_rgba(0,0,0,0.08)
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp)) // Радиус 20dp согласно Figma
            .border(
                width = 2.dp, // Белая обводка 2dp
                color = Color(0x66FFFFFF), // rgba(255,255,255,0.4) - белая с прозрачностью 40%
                shape = RoundedCornerShape(20.dp)
            )
            .background(Color(0x59000000)) // Черный фон с прозрачностью 35%
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    radius = 40.dp,
                    color = Color.White.copy(alpha = 0.3f)
                ),
                enabled = !isProcessing
            ) { 
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick() 
            },
        contentAlignment = Alignment.Center
    ) {
        // Белый фон внутри с анимированным отступом (идентично CaptureButton)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding.dp) // Анимированный отступ: 4dp -> 6dp
                .clip(RoundedCornerShape(16.dp)) // Радиус 16dp внутри
                .background(AppColors.White), // Белый фон как у CaptureButton
            contentAlignment = Alignment.Center 
        ) {
            if (isProcessing) {
                // Показываем индикатор загрузки
                Text(
                    text = "...",
                    style = AppTypography.head1,
                    color = AppColors.Black,
                    textAlign = TextAlign.Center
                )
            } else {
                // Показываем иконку галочки и текст "SAVE IMAGE"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(com.raux.myapplication_32.R.drawable.ic_save),
                        contentDescription = "Save Image",
                        modifier = Modifier.size(14.dp),
                        tint = AppColors.Black
                    )
                    Text(
                        text = "SAVE IMAGE",
                        style = AppTypography.body1.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}