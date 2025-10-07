package com.raux.myapplication_32.ui.animations

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier

/**
 * Анимации для кнопок
 */
@Composable
fun Modifier.buttonPressAnimation(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(150),
        label = "button_alpha"
    )
    
    return this.graphicsLayer(
        scaleX = scale,
        scaleY = scale,
        alpha = alpha
    )
}

/**
 * Анимация цвета для кнопок
 */
@Composable
fun animateButtonColor(
    isPressed: Boolean,
    normalColor: Color,
    pressedColor: Color
): Color {
    return animateColorAsState(
        targetValue = if (isPressed) pressedColor else normalColor,
        animationSpec = tween(200),
        label = "button_color"
    ).value
}

/**
 * Анимация для слайдера
 */
@Composable
fun Modifier.sliderAnimation(
    isDragging: Boolean
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "slider_scale"
    )
    
    return this.graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )
}

/**
 * Анимация для табов
 */
@Composable
fun Modifier.tabAnimation(
    isActive: Boolean
): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tab_scale"
    )
    
    return this.graphicsLayer(
        scaleX = scale,
        scaleY = scale
    )
}

/**
 * Анимации переходов между экранами
 */
object ScreenTransitions {
    val slideInFromBottom = slideInVertically(
        initialOffsetY = { it },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeIn(
        animationSpec = tween(300)
    )
    
    val slideOutToBottom = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeOut(
        animationSpec = tween(300)
    )
    
    val slideInFromRight = slideInVertically(
        initialOffsetY = { -it / 3 },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeIn(
        animationSpec = tween(300)
    )
    
    val slideOutToLeft = slideOutVertically(
        targetOffsetY = { it / 3 },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeOut(
        animationSpec = tween(300)
    )
}
