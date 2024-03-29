package com.example.russiansignlanguagedetection.detection

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

typealias ClassifierResult = (results: List<Classifications>?) -> Unit

class SignClassifierUseCase(
    private val context: Context,
    private val classifierResult: ClassifierResult?
) {

    companion object {
        private const val NUM_THREADS = 2
        private const val MIN_PERCENT = 0.7f
        private const val MAX_RESULT = 3

        private const val IMAGE_SIZE = 224

        private const val MODEL_PATH = "model_sign_detection_metadata.tflite"
    }

    private var gestureClassifier: ImageClassifier? = null

    init {
        initSignClassifier()
    }

    private fun initSignClassifier() {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(MIN_PERCENT) // 70%
            .setMaxResults(MAX_RESULT)

        val baseOptionsBuilder = BaseOptions.builder()
            .setNumThreads(NUM_THREADS) // 2 thread
            .useNnapi() // NNApi

        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        runCatching {
            gestureClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                MODEL_PATH, // neural network path (from assets/)
                optionsBuilder.build()
            )
        }
    }

    fun classify(image: Bitmap, imageRotation: Int) {
        gestureClassifier?.let { classifier ->
            val imageProcessor = ImageProcessor.Builder()
                .add(Rot90Op(imageRotation / 90))
                .add(ResizeOp(IMAGE_SIZE, IMAGE_SIZE, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .build()

            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))
            val results = classifier.classify(tensorImage)

            classifierResult?.invoke(results)
        } ?: run {
            initSignClassifier()
        }
    }

}
