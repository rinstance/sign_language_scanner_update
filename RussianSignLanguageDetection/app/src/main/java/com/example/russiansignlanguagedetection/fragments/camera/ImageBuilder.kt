package com.example.russiansignlanguagedetection.fragments.camera

import android.graphics.Bitmap
import androidx.camera.core.*
import java.util.concurrent.ExecutorService

class ImageBuilder {

    fun buildPreview(rotation: Int): Preview =
        Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

    fun buildImageAnalyzer(
        rotation: Int,
        cameraExecutor: ExecutorService?,
        action: (ImageProxy) -> Unit
    ): UseCase =
        ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also { imageAnalysis ->
                cameraExecutor?.let { executor ->
                    imageAnalysis.setAnalyzer(executor) { image ->
                        action.invoke(image)
                    }
                }
            }

}