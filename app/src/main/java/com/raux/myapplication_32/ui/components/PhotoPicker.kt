package com.raux.myapplication_32.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Компонент для выбора фотографий из галереи
 */
@Composable
fun rememberPhotoPickerLauncher(
    onImageSelected: (Uri) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    uri?.let { onImageSelected(it) }
}
