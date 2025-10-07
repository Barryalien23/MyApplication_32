package com.raux.myapplication_32.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raux.myapplication_32.data.models.ColorState
import com.raux.myapplication_32.data.models.SymbolPaint
import com.raux.myapplication_32.ui.components.*
import com.raux.myapplication_32.ui.theme.*

/**
 * Экран выбора цветов
 */
@Composable
fun ColorPickerScreen(
    colorState: ColorState,
    selectedMode: String?,
    onModeSelected: (String) -> Unit,
    onBackgroundColorChanged: (Color) -> Unit,
    onSymbolColorChanged: (Color) -> Unit,
    onGradientChanged: (Color, Color) -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок
            Text(
                text = "ВЫБОР ЦВЕТОВ",
                style = AppTypography.head1,
                color = AppColors.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Режимы выбора
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModeButton(
                    mode = "SOLID",
                    isSelected = selectedMode == "SOLID",
                    onClick = { onModeSelected("SOLID") },
                    modifier = Modifier.weight(1f)
                )
                ModeButton(
                    mode = "GRADIENT",
                    isSelected = selectedMode == "GRADIENT",
                    onClick = { onModeSelected("GRADIENT") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Цветовая палитра
            ColorPalette(
                onColorSelected = { color ->
                    when (selectedMode) {
                        "SOLID" -> onSymbolColorChanged(color)
                        "GRADIENT" -> {
                            val currentSymbols = colorState.symbols
                            if (currentSymbols is SymbolPaint.Gradient) {
                                onGradientChanged(color, currentSymbols.end)
                            } else {
                                onGradientChanged(color, color)
                            }
                        }
                    }
                }
            )
            
            // Кнопка фона
            Text(
                text = "ЦВЕТ ФОНА",
                style = AppTypography.body1,
                color = AppColors.White,
                modifier = Modifier.fillMaxWidth()
            )
            
            ColorPalette(
                onColorSelected = onBackgroundColorChanged
            )
            
            // Кнопка "Назад"
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.White,
                    contentColor = AppColors.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "НАЗАД",
                    style = AppTypography.body2,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Кнопка режима
 */
@Composable
fun ModeButton(
    mode: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) AppColors.White20 else AppColors.MainGrey
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mode,
            style = AppTypography.body1,
            color = AppColors.White
        )
    }
}

/**
 * Цветовая палитра
 */
@Composable
fun ColorPalette(
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color.White,
        Color.Black,
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color(0xFFFF6B6B), // Красный
        Color(0xFF4ECDC4), // Бирюзовый
        Color(0xFF45B7D1), // Синий
        Color(0xFF96CEB4), // Зеленый
        Color(0xFFFECA57), // Желтый
        Color(0xFFFF9FF3), // Розовый
        Color(0xFF54A0FF), // Голубой
        Color(0xFF5F27CD)  // Фиолетовый
    )
    
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors.size) { index ->
            val color = colors[index]
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(color) }
            )
        }
    }
}
