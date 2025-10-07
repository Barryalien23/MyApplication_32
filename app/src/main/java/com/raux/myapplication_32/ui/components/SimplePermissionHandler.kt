package com.raux.myapplication_32.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.raux.myapplication_32.ui.theme.*

@Composable
fun SimpleCameraPermissionHandler(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        println("Permission result: $isGranted")
    }
    
    LaunchedEffect(hasPermission) {
        println("Permission state changed: $hasPermission")
    }
    
    if (hasPermission) {
        content()
    } else {
        PermissionRequest(
            onRequestPermission = {
                println("Requesting camera permission...")
                launcher.launch(Manifest.permission.CAMERA)
            }
        )
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
                text = "Для работы приложения необходимо разрешение на доступ к камере",
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
