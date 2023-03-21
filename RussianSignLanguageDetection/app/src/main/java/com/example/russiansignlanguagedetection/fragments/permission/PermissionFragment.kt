package com.example.russiansignlanguagedetection.fragments.permission

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.russiansignlanguagedetection.ActivityActions
import com.example.russiansignlanguagedetection.R
import kotlinx.android.synthetic.main.fragment_permission.*

class PermissionFragment : Fragment(R.layout.fragment_permission) {

    companion object {
        fun getInstance() = PermissionFragment()
    }

    private val cameraRequestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) toSignDetectionFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        showCameraPermission()
    }

    private fun initView() {
        continue_button.setOnClickListener {
            showCameraPermission()
        }
    }

    private fun showCameraPermission() {
        cameraRequestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun toSignDetectionFragment() {
        (activity as? ActivityActions)?.toSignDetectionFragment()
    }

}
