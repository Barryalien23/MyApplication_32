package com.raux.myapplication_32.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
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
                    onEffectClick = { viewModel.navigateTo(Screen.EFFECT_PICKER) },
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
                        viewModel.selectEffect(effect)
                        viewModel.navigateBack()
                    },
                    onBackClick = { viewModel.navigateBack() }
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
                                is SymbolPaint.Gradient -> symbols.start
                            }
                        },
                        fontSize = asciiFontSize,
                        // Кнопки камеры
                        cameraFacing = cameraFacing,
                        captureState = captureState,
                        onToggleCamera = { viewModel.toggleCamera() },
                        onCapturePhoto = { viewModel.capturePhoto() }
                    )
                }
                    Screen.COLOR_PICKER -> {
                        ColorPickerScreen(
                            colorState = colorState,
                            selectedMode = when (colorState.symbols) {
                                is SymbolPaint.Solid -> "SOLID"
                                is SymbolPaint.Gradient -> "GRADIENT"
                            },
                            onModeSelected = { mode ->
                                when (mode) {
                                    "SOLID" -> viewModel.updateSymbolColor(AppColors.White)
                                    "GRADIENT" -> viewModel.updateSymbolGradient(AppColors.White, AppColors.Black)
                                }
                            },
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
                            is SymbolPaint.Gradient -> symbols.start
                        },
                        fontSize = asciiFontSize,
                        modifier = Modifier.fillMaxSize()
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = asciiText, // Убрали ограничение длины текста
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = fontSize.sp, // Используем динамический размер шрифта
            textAlign = TextAlign.Center,
            lineHeight = (fontSize * 1.1f).sp, // Более плотная высота строки
            maxLines = Int.MAX_VALUE // Убрали ограничение количества строк
        )
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
    onBackClick: () -> Unit
) {
    // Модальное окно с ограниченной высотой - показываем эффект под собой
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF5E5E5E)),  // Серый фон как основной экран
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Ограниченная высота
                .background(Color.Black)  // Черная панель эффектов
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок с кнопкой назад
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ВЫБОР ЭФФЕКТА",
                    style = AppTypography.head1,
                    color = AppColors.White
                )
                
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
                        contentDescription = "Назад",
                        tint = AppColors.White
                    )
                }
            }
            
            // Горизонтальный список эффектов
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(EffectType.values().size) { index ->
                    val effect = EffectType.values()[index]
                    EffectButton(
                        effectName = effect.displayName,
                        icon = ImageVector.vectorResource(effect.iconRes),
                        isSelected = currentEffect == effect,
                        onClick = { onEffectSelected(effect) },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    }
}
