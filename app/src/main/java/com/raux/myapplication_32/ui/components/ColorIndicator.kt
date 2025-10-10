package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.raux.myapplication_32.data.models.SymbolPaint
import com.raux.myapplication_32.data.models.ColorState
import com.raux.myapplication_32.ui.theme.AppColors

/**
 * Индикатор цвета для табов
 * Отображает выбранный цвет в виде маленького кружка
 */
@Composable
fun ColorIndicator(
    colorState: ColorState,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val color = when (val symbols = colorState.symbols) {
        is SymbolPaint.Solid -> symbols.color
        is SymbolPaint.Gradient -> symbols.start
    }
    
    val alpha = if (isSelected) 1f else 0.4f
    
    Box(
        modifier = modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = 1.dp,
                color = AppColors.White.copy(alpha = alpha),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Для градиента показываем специальный индикатор
        if (colorState.symbols is SymbolPaint.Gradient) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                (colorState.symbols as SymbolPaint.Gradient).start,
                                (colorState.symbols as SymbolPaint.Gradient).end
                            )
                        )
                    )
            )
        }
    }
}
