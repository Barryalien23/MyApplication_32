package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.raux.myapplication_32.ui.theme.*

/**
 * Панель выбора цветов
 */
@Composable
fun ColorPanel(
    colorState: ColorState,
    onBackgroundClick: () -> Unit,
    onColor1Click: () -> Unit,
    onGradientClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Кнопка цвета фона
        ColorButton(
            colorName = "BG COLOR",
            selectedColor = colorState.background,
            onClick = onBackgroundClick,
            modifier = Modifier.weight(1f)
        )
        
        // Кнопка цвета символов
        ColorButton(
            colorName = "COLOR #1",
            selectedColor = when (val symbols = colorState.symbols) {
                is com.raux.myapplication_32.data.models.SymbolPaint.Solid -> symbols.color
                is com.raux.myapplication_32.data.models.SymbolPaint.Gradient -> symbols.start
            },
            onClick = onColor1Click,
            modifier = Modifier.weight(1f)
        )
        
        // Кнопка градиента
        GradientButton(
            isGradient = colorState.symbols is com.raux.myapplication_32.data.models.SymbolPaint.Gradient,
            onClick = onGradientClick,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Кнопка выбора цвета
 */
@Composable
fun ColorButton(
    colorName: String,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.MainGrey)
            .clickable { onClick() }
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
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = AppColors.White,
                        shape = CircleShape
                    )
                    .background(selectedColor, CircleShape)
            )
            
            Text(
                text = colorName,
                style = AppTypography.body1,
                color = AppColors.White,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Кнопка градиента
 */
@Composable
fun GradientButton(
    isGradient: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isGradient) AppColors.White20 else AppColors.MainGrey
            )
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Иконка градиента
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                            colors = listOf(
                                AppColors.White,
                                AppColors.White40
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            Text(
                text = "GRADIENT",
                style = AppTypography.body1,
                color = AppColors.White,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}