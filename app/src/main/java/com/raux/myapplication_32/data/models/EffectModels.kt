package com.raux.myapplication_32.data.models

import androidx.compose.runtime.Stable
import androidx.annotation.DrawableRes
import com.raux.myapplication_32.R

/**
 * Типы визуальных эффектов
 */
enum class EffectType(val displayName: String, @DrawableRes val iconRes: Int) {
    ASCII("ASCII", R.drawable.ic_effect_ascii),
    SHAPES("SHAPES", R.drawable.ic_effect_shapes), 
    CIRCLES("CIRCLES", R.drawable.ic_effect_circles),
    SQUARES("SQUARES", R.drawable.ic_effect_squares),
    TRIANGLE("TRIANGLE", R.drawable.ic_effect_triangle),
    DIAMONDS("DIAMONDS", R.drawable.ic_effect_diamonds)
}

/**
 * Параметры эффекта (все в диапазоне 0..100)
 */
@Stable
data class EffectParams(
    val cell: Int = 50,    // Размер сетки
    val jitter: Int = 25,   // Скорость анимации
    val softy: Int = 25     // Размытие/смягчение
) {
    init {
        require(cell in 0..100) { "Cell must be in range 0..100" }
        require(jitter in 0..100) { "Jitter must be in range 0..100" }
        require(softy in 0..100) { "Softy must be in range 0..100" }
    }
}
