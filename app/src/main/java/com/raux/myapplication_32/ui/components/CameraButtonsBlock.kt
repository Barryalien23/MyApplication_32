package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.raux.myapplication_32.R
import com.raux.myapplication_32.data.models.CameraFacing
import com.raux.myapplication_32.data.models.CaptureState

/**
 * Блок кнопок управления камерой
 * Содержит: Upload, Capture, Flip
 */
@Composable
fun CameraButtonsBlock(
    cameraFacing: CameraFacing,
    captureState: CaptureState,
    isPhotoMode: Boolean = false,
    onToggleCamera: () -> Unit,
    onCapturePhoto: () -> Unit,
    onLoadPhoto: () -> Unit = {},
    onSaveProcessedImage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Кнопка загрузки из галереи
        FunctionButton(
            icon = ImageVector.vectorResource(R.drawable.ic_upload),
            onClick = onLoadPhoto
        )
        
        // Кнопка захвата фото или сохранения обработанного изображения
        if (isPhotoMode) {
            // В режиме фото показываем кнопку "Save Image"
            SaveImageButton(
                onClick = onSaveProcessedImage,
                isProcessing = captureState is CaptureState.Capturing
            )
        } else {
            // В обычном режиме показываем кнопку захвата
            CaptureButton(
                onClick = onCapturePhoto,
                isCapturing = captureState is CaptureState.Capturing
            )
        }
        
        // Кнопка переключения камеры (скрываем в режиме фото)
        if (!isPhotoMode) {
            FunctionButton(
                icon = ImageVector.vectorResource(R.drawable.ic_camera_flip),
                onClick = onToggleCamera
            )
        } else {
            // Пустое пространство для сохранения симметрии
            Spacer(modifier = Modifier.size(56.dp))
        }
    }
}
