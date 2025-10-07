package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raux.myapplication_32.R
import com.raux.myapplication_32.ui.animations.sliderAnimation
import com.raux.myapplication_32.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

/**
 * Слайдер для настройки параметров эффекта согласно дизайну Figma
 * Высота 76dp, слева процент, справа стрелка назад, интерактивный прогресс-бар
 */
@Composable
fun EffectParameterSlider(
    paramName: String,
    icon: ImageVector,
    value: Int, // 0..100
    onValueChange: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var lastUpdateTime by remember { mutableStateOf(0L) }
    var lastValue by remember { mutableStateOf(value) }
    
    // Анимация для плавного изменения прогресс-бара
    val animatedProgress by animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = tween(
            durationMillis = if (isDragging) 0 else 150, // Без задержки при перетаскивании
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "progress_animation"
    )
    
    // Throttling для плавности - обновляем не чаще чем раз в 32ms (30fps) при перетаскивании
    val throttledOnValueChange = remember {
        { newValue: Int ->
            val currentTime = System.currentTimeMillis()
            val throttleDelay = if (isDragging) 32 else 16 // Меньше обновлений при перетаскивании
            if (currentTime - lastUpdateTime >= throttleDelay) {
                lastUpdateTime = currentTime
                lastValue = newValue
                onValueChange(newValue)
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.MainGrey)
            .sliderAnimation(isDragging)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Обработка клика - устанавливаем значение на основе позиции клика
                    val newValue = (offset.x / size.width * 100).toInt()
                    val clampedValue = max(0, min(100, newValue))
                    onValueChange(clampedValue)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                        lastUpdateTime = System.currentTimeMillis()
                    },
                    onDragEnd = { 
                        isDragging = false
                        // При окончании перетаскивания принудительно обновляем значение
                        onValueChange(lastValue)
                    }
                ) { change, _ ->
                    // Обработка перетаскивания с throttling
                    val newValue = (change.position.x / size.width * 100).toInt()
                    val clampedValue = max(0, min(100, newValue))
                    throttledOnValueChange(clampedValue)
                }
            }
    ) {
        // Прогресс-бар (фон) - заполняет всю высоту, но только часть ширины
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(if (isDragging) value / 100f else animatedProgress)
                .background(Color(0xFF252525)) // grey_active из дизайна
                .align(Alignment.CenterStart)
        )
        
        // Контент поверх прогресс-бара
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Процент слева
            Text(
                text = value.toString(),
                style = AppTypography.body1.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.White,
                textAlign = TextAlign.Center
            )
            
            // Стрелка назад справа
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                    contentDescription = "Назад",
                    tint = AppColors.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
