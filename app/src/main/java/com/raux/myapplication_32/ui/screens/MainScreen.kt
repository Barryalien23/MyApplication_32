package com.raux.myapplication_32.ui.screens

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raux.myapplication_32.R
import com.raux.myapplication_32.data.models.*
import com.raux.myapplication_32.ui.components.*
import com.raux.myapplication_32.ui.effects.EffectRenderer
import com.raux.myapplication_32.ui.theme.*
import com.raux.myapplication_32.viewmodel.MainViewModel
import com.raux.myapplication_32.viewmodel.Screen

/**
 * Главный экран приложения
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val cameraFacing by viewModel.cameraFacing.collectAsStateWithLifecycle()
    val captureState by viewModel.captureState.collectAsStateWithLifecycle()
    val currentEffect by viewModel.currentEffect.collectAsStateWithLifecycle()
    val effectParams by viewModel.effectParams.collectAsStateWithLifecycle()
    val colorState by viewModel.colorState.collectAsStateWithLifecycle()
    val asciiResult by viewModel.asciiResult.collectAsStateWithLifecycle()
    val asciiFontSize by viewModel.asciiFontSize.collectAsStateWithLifecycle()
    val selectedEffectParam by viewModel.selectedEffectParam.collectAsStateWithLifecycle()
    val hapticFeedback = LocalHapticFeedback.current
    
    BasicCameraPermissionHandler {
        when (currentScreen) {
            Screen.MAIN -> {
                MainScreenContent(
                    viewModel = viewModel,
                    cameraFacing = cameraFacing,
                    captureState = captureState,
                    currentEffect = currentEffect,
                    effectParams = effectParams,
                    colorState = colorState,
                    asciiResult = asciiResult,
                    asciiFontSize = asciiFontSize,
                    onToggleCamera = { viewModel.toggleCamera() },
                    onCapturePhoto = { viewModel.capturePhoto() },
                    onEffectClick = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.navigateTo(Screen.EFFECT_PICKER) 
                    },
                    onEffectSettingsClick = { viewModel.navigateTo(Screen.EFFECT_SETTINGS) },
                    onParamClick = { paramName -> viewModel.navigateToEffectSettings(paramName) },
                    onColorClick = { viewModel.navigateTo(Screen.COLOR_PICKER) },
                    modifier = modifier
                )
            }
            Screen.EFFECT_PICKER -> {
                EffectPickerScreen(
                    currentEffect = currentEffect,
                    onEffectSelected = { effect ->
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.selectEffect(effect)
                        viewModel.navigateBack()
                    },
                    onBackClick = { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.navigateBack() 
                    },
                    // ASCII-эффект для отображения в реальном времени
                    asciiText = asciiResult,
                    backgroundColor = colorState.background,
                    textColor = run {
                        val symbols = colorState.symbols
                        when (symbols) {
                            is SymbolPaint.Solid -> symbols.color
                            is SymbolPaint.Gradient -> Color.Unspecified // Специальный маркер для градиента
                        }
                    },
                    colorState = colorState,
                    // Кнопки камеры
                    cameraFacing = cameraFacing,
                    captureState = captureState,
                    onToggleCamera = { viewModel.toggleCamera() },
                    onCapturePhoto = { viewModel.capturePhoto() }
                )
            }
                Screen.EFFECT_SETTINGS -> {
                    EffectSettingsScreen(
                        params = effectParams,
                        selectedParam = selectedEffectParam,
                        onParamSelected = { paramName ->
                            viewModel.navigateToEffectSettings(paramName)
                        },
                        onParamValueChanged = { paramName, value ->
                            val newParams = when (paramName) {
                                "Cell" -> effectParams.copy(cell = value)
                                "Jitter" -> effectParams.copy(jitter = value)
                                "Softy" -> effectParams.copy(softy = value)
                                else -> effectParams
                            }
                            viewModel.updateEffectParams(newParams)
                        },
                        onBackClick = { viewModel.navigateBack() },
                        // ASCII-эффект для отображения в реальном времени
                        asciiText = asciiResult,
                        backgroundColor = colorState.background,
                        textColor = run {
                            val symbols = colorState.symbols
                            when (symbols) {
                                is SymbolPaint.Solid -> symbols.color
                                is SymbolPaint.Gradient -> Color.Unspecified // Специальный маркер для градиента
                            }
                        },
                        fontSize = asciiFontSize,
                        colorState = colorState, // Передаем ColorState для градиента
                        // Кнопки камеры
                        cameraFacing = cameraFacing,
                        captureState = captureState,
                        onToggleCamera = { viewModel.toggleCamera() },
                        onCapturePhoto = { viewModel.capturePhoto() }
                    )
                }
                    Screen.COLOR_PICKER -> {
                        ColorPickerBlockScreen(
                            colorState = colorState,
                            asciiText = asciiResult,
                            asciiFontSize = asciiFontSize,
                            onBackgroundColorChanged = viewModel::updateBackgroundColor,
                            onSymbolColorChanged = viewModel::updateSymbolColor,
                            onGradientChanged = { start, end -> viewModel.updateSymbolGradient(start, end) },
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }
        }
    }
}

@Composable
private fun MainScreenContent(
    viewModel: MainViewModel,
    cameraFacing: CameraFacing,
    captureState: CaptureState,
    currentEffect: EffectType,
    effectParams: EffectParams,
    colorState: ColorState,
    asciiResult: String,
    asciiFontSize: Float,
    onToggleCamera: () -> Unit,
    onCapturePhoto: () -> Unit,
    onEffectClick: () -> Unit,
    onEffectSettingsClick: () -> Unit,
    onParamClick: (String) -> Unit,
    onColorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Область ASCII эффекта (вместо камеры)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(colorState.background)  // Фон из настроек цвета
            ) {
                // Показываем ASCII эффект на весь экран
                if (asciiResult.isNotEmpty()) {
                    ASCIIPreview(
                        asciiText = asciiResult,
                        backgroundColor = colorState.background,
                        textColor = when (val symbols = colorState.symbols) {
                            is SymbolPaint.Solid -> symbols.color
                            is SymbolPaint.Gradient -> Color.Unspecified // Специальный маркер для градиента
                        },
                        fontSize = asciiFontSize,
                        modifier = Modifier.fillMaxSize(),
                        colorState = colorState // Передаем ColorState для градиента
                    )
                } else {
                    // Показываем загрузку
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ЗАГРУЗКА ЭФФЕКТА...",
                            style = AppTypography.head1,
                            color = AppColors.White
                        )
                    }
                }
                
                // Скрытая камера для обработки изображений (невидимая)
                CameraPreview(
                    cameraFacing = cameraFacing,
                    modifier = Modifier.fillMaxSize().alpha(0f), // Полностью прозрачная
                    onImageAnalysis = { imageProxy ->
                        viewModel.processCameraImage(imageProxy, screenWidth, screenHeight, 1f)
                    }
                )
                
                // Кнопки камеры поверх ASCII эффекта (8dp от MainSettingsPanel)
                CameraButtonsBlock(
                    cameraFacing = cameraFacing,
                    captureState = captureState,
                    onToggleCamera = onToggleCamera,
                    onCapturePhoto = onCapturePhoto,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            
            // Панель настроек эффекта внизу экрана
            MainSettingsPanel(
                currentEffect = currentEffect,
                effectParams = effectParams,
                colorState = colorState,
                onEffectClick = onEffectClick,
                onEffectSettingsClick = onEffectSettingsClick,
                onParamClick = onParamClick,
                onColorClick = onColorClick,
                modifier = Modifier.padding(bottom = 16.dp) // Отступ от края экрана
            )
        }
    }
}

@Composable
fun ASCIIPreview(
    asciiText: String,
    backgroundColor: Color,
    textColor: Color,
    fontSize: Float = 16f,
    modifier: Modifier = Modifier,
    colorState: ColorState? = null // Добавляем ColorState для градиента
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Если textColor - это градиент, используем специальную версию
        if (textColor == Color.Unspecified && colorState != null) {
            // Это означает, что нужно использовать градиент
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
                fontSize = fontSize.sp,
                textAlign = TextAlign.Start,
                lineHeight = (fontSize * 1.1f).sp,
                maxLines = Int.MAX_VALUE
            )
        }
    }
}

@Composable
private fun ASCIIPreviewWithGradient(
    asciiText: String,
    fontSize: Float,
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
                fontSize = fontSize.sp,
                textAlign = TextAlign.Start,
                lineHeight = (fontSize * 1.1f).sp
            )
        }
    }
}

@Composable
private fun ControlPanel(
    cameraFacing: CameraFacing,
    captureState: CaptureState,
    currentEffect: EffectType,
    effectParams: EffectParams,
    colorState: ColorState,
    onToggleCamera: () -> Unit,
    onCapturePhoto: () -> Unit,
    onEffectClick: () -> Unit,
    onEffectSettingsClick: () -> Unit,
    onParamClick: (String) -> Unit,
    onColorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Блок кнопок камеры (отдельно)
        CameraButtonsBlock(
            cameraFacing = cameraFacing,
            captureState = captureState,
            onToggleCamera = onToggleCamera,
            onCapturePhoto = onCapturePhoto
        )
        
        // Панель настроек эффекта (отдельно)
        MainSettingsPanel(
            currentEffect = currentEffect,
            effectParams = effectParams,
            colorState = colorState,
            onEffectClick = onEffectClick,
            onEffectSettingsClick = onEffectSettingsClick,
            onParamClick = onParamClick,
            onColorClick = onColorClick
        )
    }
}


@Composable
private fun EffectPickerScreen(
    currentEffect: EffectType,
    onEffectSelected: (EffectType) -> Unit,
    onBackClick: () -> Unit,
    // ASCII-эффект для отображения в реальном времени
    asciiText: String,
    backgroundColor: Color,
    textColor: Color,
    colorState: ColorState,
    // Кнопки камеры
    cameraFacing: CameraFacing,
    captureState: CaptureState,
    onToggleCamera: () -> Unit,
    onCapturePhoto: () -> Unit
) {
    // Полноэкранный режим с ASCII-эффектом и выбором эффектов поверх
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ASCII-эффект на весь экран (как на главном экране)
        ASCIIPreview(
            asciiText = asciiText,
            backgroundColor = backgroundColor,
            textColor = textColor,
            colorState = colorState,
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
        
        // Панель выбора эффектов внизу экрана (точно как MainSettingsPanel)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(160.dp) // 144dp (MainSettingsPanel) + 16dp (дополнительный отступ)
                .background(Color.Black)  // Черная панель эффектов
                .padding(12.dp) // Точно такой же padding как в MainSettingsPanel
                .padding(bottom = 16.dp), // Точно такой же дополнительный отступ как у MainSettingsPanel
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Горизонтальный скролл с табами эффектов
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                items(EffectType.values().size) { index ->
                    val effect = EffectType.values()[index]
                    val isSelected = effect == currentEffect
                    
                    Box(
                        modifier = Modifier
                            .width(88.dp) // Фиксированная ширина для табов (+8dp)
                            .height(120.dp) // Высота содержимого без padding (160dp - 12dp - 16dp - 12dp = 120dp)
                            .clip(RoundedCornerShape(12.dp)) // Такой же радиус как в MainSettingsPanel
                            .background(
                                if (isSelected) AppColors.GreyActive else AppColors.MainGrey
                            )
                            .clickable { onEffectSelected(effect) }
                            .padding(12.dp), // Такой же padding как в MainSettingsPanel
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
                                modifier = Modifier.size(16.dp) // Такая же иконка как в MainSettingsPanel
                            )
                            
                            Text(
                                text = effect.displayName,
                                style = AppTypography.body1.copy(
                                    fontSize = 12.sp, // Такой же размер как в MainSettingsPanel
                                    fontWeight = FontWeight.Medium
                                ),
                                color = AppColors.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPickerBlockScreen(
    colorState: ColorState,
    asciiText: String,
    asciiFontSize: Float,
    onBackgroundColorChanged: (Color) -> Unit,
    onSymbolColorChanged: (Color) -> Unit,
    onGradientChanged: (Color, Color) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(ColorTab.BACKGROUND) }
    
    // Полноэкранный режим с ASCII-эффектом и блоком выбора цвета поверх
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // ASCII-эффект на весь экран (как на главном экране)
        ASCIIPreview(
            asciiText = asciiText,
            backgroundColor = colorState.background,
            textColor = run {
                val symbols = colorState.symbols
                when (symbols) {
                    is SymbolPaint.Solid -> symbols.color
                    is SymbolPaint.Gradient -> Color.Unspecified // Специальный маркер для градиента
                }
            },
            colorState = colorState,
            fontSize = asciiFontSize,
            modifier = Modifier.fillMaxSize()
        )

        // Блок выбора цвета внизу экрана
        ColorPickerBlock(
            colorState = colorState,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onBackgroundColorChanged = onBackgroundColorChanged,
            onSymbolColorChanged = onSymbolColorChanged,
            onGradientChanged = onGradientChanged,
            onBackClick = onBackClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
