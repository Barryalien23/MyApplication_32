package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raux.myapplication_32.R
import com.raux.myapplication_32.data.models.*
import com.raux.myapplication_32.ui.animations.buttonPressAnimation
import com.raux.myapplication_32.ui.theme.*

/**
 * Главная панель настроек согласно дизайну Figma
 * Структура: слева кнопка эффекта, справа две строки настроек
 */
@Composable
fun MainSettingsPanel(
    currentEffect: EffectType,
    effectParams: EffectParams,
    colorState: ColorState,
    onEffectClick: () -> Unit,
    onEffectSettingsClick: () -> Unit,
    onParamClick: (String) -> Unit,
    onColorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(144.dp) // Фиксированная высота как в дизайне
            .background(Color.Black)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Слева: кнопка выбранного эффекта (вертикальная)
        EffectButtonVertical(
            effect = currentEffect,
            onClick = onEffectClick,
            modifier = Modifier
                .width(64.dp)
                .height(120.dp)
        )
        
        // Справа: две строки настроек
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Первая строка: CELL, JITTER, SOFTY
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainSettingButton(
                    paramName = "CELL",
                    icon = ImageVector.vectorResource(R.drawable.ic_setting_cell),
                    value = effectParams.cell,
                    onClick = { onParamClick("Cell") },
                    modifier = Modifier.weight(1f)
                )
                MainSettingButton(
                    paramName = "JITTER",
                    icon = ImageVector.vectorResource(R.drawable.ic_setting_jitter),
                    value = effectParams.jitter,
                    onClick = { onParamClick("Jitter") },
                    modifier = Modifier.weight(1f)
                )
                MainSettingButton(
                    paramName = "SOFTY",
                    icon = ImageVector.vectorResource(R.drawable.ic_setting_softy),
                    value = effectParams.softy,
                    onClick = { onParamClick("Softy") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Вторая строка: BG COLOR, COLOR #1, GRADIENT
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MainColorButton(
                    colorName = "BG COLOR",
                    selectedColor = colorState.background,
                    onClick = onColorClick,
                    modifier = Modifier.weight(1f)
                )
                MainColorButton(
                    colorName = "COLOR #1",
                    selectedColor = when (val symbols = colorState.symbols) {
                        is SymbolPaint.Solid -> symbols.color
                        is SymbolPaint.Gradient -> symbols.start
                    },
                    onClick = onColorClick,
                    modifier = Modifier.weight(1f)
                )
                MainGradientButton(
                    isGradient = colorState.symbols is SymbolPaint.Gradient,
                    onClick = onColorClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Вертикальная кнопка эффекта (слева)
 */
@Composable
private fun EffectButtonVertical(
    effect: EffectType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .buttonPressAnimation(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(effect.iconRes),
                contentDescription = effect.displayName,
                tint = AppColors.White,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = effect.displayName,
                style = AppTypography.body1.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Кнопка настройки (CELL, JITTER, SOFTY) с вертикальной полоской прогресса слева
 */
@Composable
private fun MainSettingButton(
    paramName: String,
    icon: ImageVector,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .buttonPressAnimation(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
    ) {
        // Полоска прогресса слева - заполняет всю высоту, но только часть ширины
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(value / 100f)
                .background(Color(0xFF252525)) // grey_active из дизайна
                .align(Alignment.CenterStart)
        )
        
        // Иконка и текст по центру кнопки
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = paramName,
                tint = AppColors.White,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = paramName,
                style = AppTypography.body1.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Кнопка цвета
 */
@Composable
private fun MainColorButton(
    colorName: String,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .buttonPressAnimation(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(selectedColor)
            )
            
            Text(
                text = colorName,
                style = AppTypography.body1.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Кнопка градиента
 */
@Composable
private fun MainGradientButton(
    isGradient: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .buttonPressAnimation(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                AppColors.White,
                                AppColors.White40
                            )
                        )
                    )
            )
            
            Text(
                text = "GRADIENT",
                style = AppTypography.body1.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
