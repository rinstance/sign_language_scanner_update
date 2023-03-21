package com.example.russiansignlanguagedetection.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Activity.checkCameraPermission(action: (Boolean) -> Unit) {
    action.invoke(
        ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    )
}
