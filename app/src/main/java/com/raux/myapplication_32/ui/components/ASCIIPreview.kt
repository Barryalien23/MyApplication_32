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
import com.raux.myapplication_32.ui.theme.*

/**
 * Компонент для отображения ASCII текста с адаптивным размером
 */
@Composable
fun ASCIIPreview(
    asciiText: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
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
