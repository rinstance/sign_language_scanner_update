package com.example.russiansignlanguagedetection.fragments.camera

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.russiansignlanguagedetection.ActivityActions
import com.example.russiansignlanguagedetection.R
import com.example.russiansignlanguagedetection.utils.checkCameraPermission
import kotlinx.android.synthetic.main.fragment_sign_detection.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class SignDetectionFragment : Fragment(R.layout.fragment_sign_detection) {

    companion object {
        fun getInstance() = SignDetectionFragment()
    }

    private val viewModel: SignDetectionViewModel by lazy {
        ViewModelProvider(this)[SignDetectionViewModel::class.java]
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCamera()
        initListeners()
        initCollectors()
    }

    private fun initListeners() {
        add_letter.setOnClickListener {
            viewModel.updateWordWithAddingLast()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initCollectors() {
        lifecycleScope.launch {
            viewModel.word.filterNotNull().collect(detection_text::setText)
        }
        lifecycleScope.launch {
            viewModel.score.filterNotNull().collect { score ->
                detection_percent.text = "$score%"
                detection_progress.setProgress(
                    score.toInt(),
                    true
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().checkCameraPermission { isGranted ->
            if (!isGranted) (activity as? ActivityActions)?.toPermissionFragment()
        }
    }

    private fun initCamera() {
        camera_preview_view.post {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener(
                {
                    cameraProvider = cameraProviderFuture.get()
                    bindCameraToLifecycle()
                },
                ContextCompat.getMainExecutor(requireContext())
            )
        }
    }

    private fun bindCameraToLifecycle() {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val rotation = camera_preview_view.display.rotation
        val preview = viewModel.buildPreview(rotation)
        val imageAnalyzer = viewModel.buildImageAnalyzer(rotation)

        cameraProvider?.unbindAll()

        runCatching {
            camera = cameraProvider?.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer
            )
            preview.setSurfaceProvider(camera_preview_view.surfaceProvider)
        }
    }

}
