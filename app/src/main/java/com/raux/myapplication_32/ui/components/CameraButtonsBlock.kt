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
    onToggleCamera: () -> Unit,
    onCapturePhoto: () -> Unit,
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
            onClick = { /* TODO: Реализовать загрузку из галереи */ }
        )
        
        // Кнопка захвата фото
        CaptureButton(
            onClick = onCapturePhoto,
            isCapturing = captureState is CaptureState.Capturing
        )
        
        // Кнопка переключения камеры
        FunctionButton(
            icon = ImageVector.vectorResource(R.drawable.ic_camera_flip),
            onClick = onToggleCamera
        )
    }
}
