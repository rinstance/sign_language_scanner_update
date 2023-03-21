package com.example.russiansignlanguagedetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.russiansignlanguagedetection.fragments.permission.PermissionFragment
import com.example.russiansignlanguagedetection.fragments.camera.SignDetectionFragment
import com.example.russiansignlanguagedetection.utils.checkCameraPermission

class MainActivity : AppCompatActivity(), ActivityActions {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
    }

    private fun checkPermission() {
        checkCameraPermission { isGranted ->
            when {
                isGranted -> toSignDetectionFragment()
                else -> toPermissionFragment()
            }
        }
    }

    override fun toPermissionFragment() {
        toFragment(PermissionFragment.getInstance())
    }

    override fun toSignDetectionFragment() {
        toFragment(SignDetectionFragment.getInstance())
    }

    private fun toFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_container, fragment)
            .commit()
    }

}