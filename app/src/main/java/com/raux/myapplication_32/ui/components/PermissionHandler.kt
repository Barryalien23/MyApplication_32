package com.raux.myapplication_32.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import com.raux.myapplication_32.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionHandler(
    content: @Composable () -> Unit
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA
        )
    )
    
    // Отслеживаем изменения разрешений
    LaunchedEffect(permissionsState.allPermissionsGranted) {
        // Этот блок будет выполняться при изменении состояния разрешений
    }
    
    // Отладочная информация
    LaunchedEffect(Unit) {
        println("Permission state: ${permissionsState.allPermissionsGranted}")
        println("Individual permissions: ${permissionsState.permissions.map { it.status }}")
    }
    
    when {
        permissionsState.allPermissionsGranted -> {
            content()
        }
        permissionsState.shouldShowRationale -> {
            PermissionRationale(
                onRequestPermission = { 
                    permissionsState.launchMultiplePermissionRequest()
                }
            )
        }
        else -> {
            PermissionRequest(
                onRequestPermission = { 
                    permissionsState.launchMultiplePermissionRequest()
                }
            )
        }
    }
}

@Composable
private fun PermissionRequest(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(Spacing.l),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            Text(
                text = "РАЗРЕШЕНИЯ КАМЕРЫ",
                style = AppTypography.head1,
                color = AppColors.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Для работы приложения необходимы разрешения на доступ к камере и галерее",
                style = AppTypography.body1,
                color = AppColors.White40,
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.White,
                    contentColor = AppColors.Black
                ),
                shape = RoundedCornerShape(Roundings.m)
            ) {
                Text(
                    text = "РАЗРЕШИТЬ",
                    style = AppTypography.body2
                )
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(Spacing.l),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            Text(
                text = "РАЗРЕШЕНИЯ ОТКЛОНЕНЫ",
                style = AppTypography.head1,
                color = AppColors.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Приложение не может работать без разрешений. Пожалуйста, разрешите доступ в настройках",
                style = AppTypography.body1,
                color = AppColors.White40,
                textAlign = TextAlign.Center
            )
            
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.White,
                    contentColor = AppColors.Black
                ),
                shape = RoundedCornerShape(Roundings.m)
            ) {
                Text(
                    text = "ПОПРОБОВАТЬ СНОВА",
                    style = AppTypography.body2
                )
            }
        }
    }
}
