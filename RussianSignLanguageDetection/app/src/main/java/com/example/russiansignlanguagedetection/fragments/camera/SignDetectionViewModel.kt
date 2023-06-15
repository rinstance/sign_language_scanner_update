package com.example.russiansignlanguagedetection.fragments.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.russiansignlanguagedetection.detection.SignClassifierUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SignDetectionViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val SAVE_LETTER_MILLIS = 2000L
    }

    private var signClassifierUseCase: SignClassifierUseCase? = null
    private var bitmapBuffer: Bitmap? = null
    private var cameraExecutor: ExecutorService? = null

    private var letterJob: Job? = null

    private val lastLetter = MutableStateFlow<String?>(null)

    private val _word = MutableStateFlow<String?>(null)
    val word: StateFlow<String?> = _word

    private val _score = MutableStateFlow<Float?>(null)
    val score: StateFlow<Float?> = _score

    private val imageBuilder = ImageBuilder()

    init {
        signClassifierUseCase = SignClassifierUseCase(
            context = getApplication(),
            classifierResult = (::handleResult)
        )
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun updateWordWithAddingLast() {
        _word.value = word.value.orEmpty() + lastLetter.value.orEmpty()
    }

    fun buildPreview(rotation: Int): Preview =
        imageBuilder.buildPreview(rotation)

    fun buildImageAnalyzer(rotation: Int): UseCase =
        imageBuilder.buildImageAnalyzer(rotation, cameraExecutor) { image ->
            if (bitmapBuffer == null) {
                bitmapBuffer = Bitmap.createBitmap(
                    image.width,
                    image.height,
                    Bitmap.Config.ARGB_8888
                )
            }

            classifyImage(image)
        }

    private fun classifyImage(image: ImageProxy) {
        image.use {
            bitmapBuffer?.copyPixelsFromBuffer(image.planes[0].buffer)
        }

        bitmapBuffer?.let { buffer ->
            val flipMatrix = Matrix().apply {
                postScale(
                    -1f,
                    1f,
                    buffer.width.toFloat(),
                    buffer.height.toFloat()
                )
            }

            val flippedBitmap = Bitmap.createBitmap(
                buffer,
                0,
                0,
                buffer.width,
                buffer.height,
                flipMatrix,
                true
            )

            signClassifierUseCase?.classify(
                image = flippedBitmap,
                imageRotation = 180 - image.imageInfo.rotationDegrees
            )
        }
    }

    private fun handleResult(classifications: List<Classifications>?) {
        runCatching {
            classifications?.get(0)?.categories?.get(0)
        }
            .onSuccess { category ->
                category?.let {
                    _word.value = word.value?.dropLast(1).orEmpty() + category.label
                    _score.value = category.score * 100
                    checkNewCategory(category)
                }
            }
            .getOrNull()
    }

    private fun checkNewCategory(category: Category) {
        val newLetter = category.label
        if (lastLetter.value != newLetter) restartLetterJob()
        lastLetter.value = newLetter
    }

    private fun restartLetterJob() {
        letterJob?.cancel()

        letterJob = viewModelScope.launch {
            delay(SAVE_LETTER_MILLIS)
            updateWordWithAddingLast()
            letterJob?.cancel()
        }
    }

    override fun onCleared() {
        cameraExecutor?.shutdown()
        super.onCleared()
    }

}
