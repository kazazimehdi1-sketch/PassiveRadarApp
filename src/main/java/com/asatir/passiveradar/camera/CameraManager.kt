package com.asatir.passiveradar.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

class CameraManager(private val context: Context) {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage: StateFlow<Bitmap?> = _capturedImage

    fun startCamera(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(android.view.Surface.ROTATION_0)
                    .build()

                val preview = Preview.Builder().build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun captureImage(outputDir: File, executor: Executor, onSuccess: (File) -> Unit, onError: (Exception) -> Unit) {
        val imageCapture = imageCapture ?: return

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val photoFile = File(outputDir, "IMG_${timeStamp}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onSuccess(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
    }
}
