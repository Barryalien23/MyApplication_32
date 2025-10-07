package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.raux.myapplication_32.data.models.CameraFacing
import com.raux.myapplication_32.data.models.CaptureState
import com.raux.myapplication_32.data.models.EffectParams
import com.raux.myapplication_32.ui.animations.tabAnimation
import com.raux.myapplication_32.ui.theme.*
import androidx.compose.ui.text.font.FontFamily
import com.raux.myapplication_32.ui.screens.ASCIIPreview
import com.raux.myapplication_32.ui.components.SettingButton
import com.raux.myapplication_32.ui.components.EffectParameterSlider
import com.raux.myapplication_32.ui.components.CameraButtonsBlock

/**
 * Параметры эффекта с иконками
 */
data class EffectParameter(
    val name: String,
    val icon: Int, // drawable resource
    val getValue: (EffectParams) -> Int,
    val displayName: String
)

val effectParameters = listOf(
    EffectParameter("Cell", R.drawable.ic_setting_cell, { it.cell }, "CELL"),
    EffectParameter("Jitter", R.drawable.ic_setting_jitter, { it.jitter }, "JITTER"),
    EffectParameter("Softy", R.drawable.ic_setting_softy, { it.softy }, "SOFTY")
)

/**
 * Экран настройки параметров эффекта с табами и слайдером
 * Показывает ASCII-эффект в реальном времени с кнопками камеры
 */
@Composable
fun EffectSettingsScreen(
    params: EffectParams,
    selectedParam: String?,
    onParamSelected: (String) -> Unit,
    onParamValueChanged: (String, Int) -> Unit,
    onBackClick: () -> Unit,
    // ASCII-эффект для отображения в реальном времени
    asciiText: String,
    backgroundColor: Color,
    textColor: Color,
    fontSize: Float,
    // Кнопки камеры
    cameraFacing: CameraFacing,
    captureState: CaptureState,
    onToggleCamera: () -> Unit,
    onCapturePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Определяем активный параметр
    val activeParam = selectedParam ?: "Cell"
    val activeParamIndex = effectParameters.indexOfFirst { it.name == activeParam }
    val currentActiveParam = if (activeParamIndex >= 0) effectParameters[activeParamIndex] else effectParameters[0]
    
    // Полноэкранный режим с ASCII-эффектом и настройками поверх
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // ASCII-эффект на весь экран (как на главном экране)
        ASCIIPreview(
            asciiText = asciiText,
            backgroundColor = backgroundColor,
            textColor = textColor,
            fontSize = fontSize,
            modifier = Modifier.fillMaxSize()
        )
        
        // Кнопки камеры поверх ASCII-эффекта
        CameraButtonsBlock(
            cameraFacing = cameraFacing,
            captureState = captureState,
            onToggleCamera = onToggleCamera,
            onCapturePhoto = onCapturePhoto,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        
        // Панель настроек эффекта внизу экрана
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(164.dp) // Фиксированная высота как в дизайне
                .background(Color.Black)  // Черная панель настроек
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Табы для переключения между параметрами
            EffectParameterTabs(
                params = params,
                activeParam = activeParam,
                onParamSelected = onParamSelected
            )
            
            // Слайдер для активного параметра
            key(activeParam) {
                EffectParameterSlider(
                    paramName = currentActiveParam.displayName,
                    icon = ImageVector.vectorResource(currentActiveParam.icon),
                    value = currentActiveParam.getValue(params),
                    onValueChange = { newValue ->
                        onParamValueChanged(currentActiveParam.name, newValue)
                    },
                    onBackClick = onBackClick
                )
            }
        }
    }
}

/**
 * Табы для переключения между параметрами эффекта
 */
@Composable
private fun EffectParameterTabs(
    params: EffectParams,
    activeParam: String,
    onParamSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        effectParameters.forEach { param ->
            val isActive = param.name == activeParam
            val value = param.getValue(params)
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isActive) AppColors.GreyActive else AppColors.GreyDisable
                    )
                    .tabAnimation(isActive)
                    .clickable { onParamSelected(param.name) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(param.icon),
                        contentDescription = param.displayName,
                        tint = if (isActive) AppColors.White else AppColors.White40,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Text(
                        text = param.displayName,
                        style = AppTypography.body1.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                        ),
                        color = if (isActive) AppColors.White else AppColors.White40
                    )
                }
            }
        }
    }
}


/**
 * Компактная панель настроек для главного экрана
 */
@Composable
fun EffectSettingsPanel(
    params: EffectParams,
    onParamClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        effectParameters.forEach { param ->
            val value = param.getValue(params)
            SettingButton(
                paramName = param.displayName,
                icon = ImageVector.vectorResource(param.icon),
                value = value,
                onClick = { onParamClick(param.name) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}