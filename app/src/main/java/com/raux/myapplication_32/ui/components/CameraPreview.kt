package com.raux.myapplication_32.ui.components

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.raux.myapplication_32.data.models.CameraFacing
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Компонент превью камеры с CameraX
 */
@Composable
fun CameraPreview(
    cameraFacing: CameraFacing,
    modifier: Modifier = Modifier,
    onImageCaptured: (ImageProxy) -> Unit = {},
    onImageAnalysis: (ImageProxy) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var previewUseCase by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var imageAnalyzer by remember { mutableStateOf<ImageAnalysis?>(null) }
    
    LaunchedEffect(cameraFacing) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()
        
        val cameraSelector = cameraFacing.toCameraSelector()
        val executor = ContextCompat.getMainExecutor(context)
        
        // Preview use case
        val preview = Preview.Builder().build()
        
        // Image capture use case  
        val imageCaptureUseCase = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        
        // Image analysis use case для обработки в реальном времени
        val imageAnalysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(executor) { imageProxy ->
                    onImageAnalysis(imageProxy)
                    imageProxy.close()
                }
            }
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCaptureUseCase,
                imageAnalysisUseCase
            )
            
            previewUseCase = preview
            imageCapture = imageCaptureUseCase
            imageAnalyzer = imageAnalysisUseCase
            
        } catch (exc: Exception) {
            // Handle camera binding errors
        }
    }
    
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            previewUseCase?.setSurfaceProvider(previewView.surfaceProvider)
        }
    )
}

/**
 * Утилиты для работы с камерой
 */
object CameraUtils {
    
    /**
     * Проверка доступности камеры
     */
    suspend fun isCameraAvailable(context: Context): Boolean = suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor = ContextCompat.getMainExecutor(context)
        
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val hasBackCamera = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                val hasFrontCamera = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                continuation.resume(hasBackCamera || hasFrontCamera)
            } catch (exc: Exception) {
                continuation.resume(false)
            }
        }, executor)
    }
    
    /**
     * Захват изображения
     */
    fun captureImage(
        imageCapture: ImageCapture,
        executor: Executor,
        onImageCaptured: (ImageProxy) -> Unit,
        onError: (Exception) -> Unit
    ) {
        imageCapture.takePicture(
            executor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    onImageCaptured(image)
                }
                
                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
}
