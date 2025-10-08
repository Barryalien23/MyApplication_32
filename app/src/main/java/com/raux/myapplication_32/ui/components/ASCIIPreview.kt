package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raux.myapplication_32.data.models.ColorState
import com.raux.myapplication_32.data.models.SymbolPaint
import com.raux.myapplication_32.ui.theme.*

/**
 * Компонент для отображения ASCII текста с адаптивным размером и поддержкой градиента
 */
@Composable
fun ASCIIPreview(
    asciiText: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    colorState: ColorState? = null // Добавляем ColorState для градиента
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    
    // Вычисляем оптимальный размер шрифта для заполнения экрана
    val lines = asciiText.split("\n")
    val maxLineLength = lines.maxOfOrNull { it.length } ?: 1
    
    // Адаптивно вычисляем размер шрифта для полного заполнения экрана
    val fontSize = when {
        screenWidth < 400 -> (screenWidth * 0.8f / maxLineLength).coerceIn(1f, 3f).sp
        screenWidth < 600 -> (screenWidth * 0.7f / maxLineLength).coerceIn(2f, 4f).sp
        screenWidth < 800 -> (screenWidth * 0.6f / maxLineLength).coerceIn(3f, 5f).sp
        else -> (screenWidth * 0.5f / maxLineLength).coerceIn(4f, 6f).sp
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Если textColor - это градиент, используем специальную версию
        if (textColor == Color.Unspecified && colorState != null) {
            ASCIIPreviewWithGradient(
                asciiText = asciiText,
                fontSize = fontSize,
                colorState = colorState
            )
        } else {
            // Обычный сплошной цвет
            Text(
                text = asciiText,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize,
                lineHeight = fontSize * 0.9f, // Уменьшаем межстрочный интервал
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
            )
        }
    }
}

@Composable
private fun ASCIIPreviewWithGradient(
    asciiText: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    colorState: ColorState
) {
    val lines = asciiText.split("\n")
    val totalLines = lines.size
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        lines.forEachIndexed { index, line ->
            // Вычисляем цвет для этой строки в градиенте
            val progress = if (totalLines > 1) index.toFloat() / (totalLines - 1) else 0f
            
            val lineColor = when (val symbols = colorState.symbols) {
                is SymbolPaint.Solid -> symbols.color
                is SymbolPaint.Gradient -> {
                    Color(
                        red = symbols.start.red + (symbols.end.red - symbols.start.red) * progress,
                        green = symbols.start.green + (symbols.end.green - symbols.start.green) * progress,
                        blue = symbols.start.blue + (symbols.end.blue - symbols.start.blue) * progress
                    )
                }
            }
            
            Text(
                text = line,
                color = lineColor,
                fontFamily = FontFamily.Monospace,
                fontSize = fontSize,
                lineHeight = fontSize * 0.9f,
                textAlign = TextAlign.Start
            )
        }
    }
}
