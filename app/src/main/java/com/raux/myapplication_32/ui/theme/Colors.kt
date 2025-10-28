package com.raux.myapplication_32.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Основные цвета из дизайна
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
    val MainGrey = Color(0xFF1A1A1A)
    val BackgroundDark = Color(0xFF272727)
    val GreyDisable = Color(0xFF141414)
    val GreyActive = Color(0xFF252525)
    
    // Прозрачности
    val White20 = Color(0x33FFFFFF) // White с 20% прозрачностью
    val White40 = Color(0x66FFFFFF) // White с 40% прозрачностью
    
    // Акцентные цвета
    val Green = Color(0xFF4CAF50) // Зеленый для кнопки сохранения
    
    // Системные цвета
    val Surface = MainGrey
    val OnSurface = White
    val Background = Color(0xFF000000)  // Чисто черный как в дизайне
    val OnBackground = White
    
    // Состояния
    val Disabled = White40
    val Active = White
    val Pressed = White20
}
