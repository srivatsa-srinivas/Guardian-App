// NewActivity.kt

@file:Suppress("DEPRECATION", "ControlFlowWithEmptyBody", "PrivatePropertyName")

package com.nikhil.guardian

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nikhil.guardian.databinding.YourNewActivityLayoutBinding // Replace with your actual binding class

class NewActivity : AppCompatActivity() {
    private lateinit var binding: YourNewActivityLayoutBinding // Replace with your actual binding class
    private var isFlashOn = false
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var blinkHandler: Handler = Handler()
    private var blinkRunnable: Runnable? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = YourNewActivityLayoutBinding.inflate(layoutInflater) // Replace with your actual binding class
        setContentView(binding.root)

        // Set click listener for the start button in the new activity
        binding.startButton.setOnClickListener {
            // Start blinking effect
            startBlinkingEffect()
        }

        // Set click listener for the stop button in the new activity
        binding.stopButton.setOnClickListener {
            // Stop blinking effect
            stopBlinkingEffect()
        }
    }

    private fun startBlinkingEffect() {
        // Implement background color change and flash lights logic here
        // Example: Change background color to red
        binding.yourBlinkingView.setBackgroundColor(Color.RED)

        // Toggle flashlight
        toggleFlashlight()

        // Start blinking effect
        blinkRunnable = object : Runnable {
            override fun run() {
                // Toggle visibility or change properties of yourBlinkingView for blinking effect
                binding.yourBlinkingView.visibility =
                    if (binding.yourBlinkingView.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE

                // Schedule the next iteration after a delay (e.g., 500 milliseconds)
                blinkHandler.postDelayed(this, 500)
            }
        }

        // Start the blinking effect immediately
        blinkHandler.post(blinkRunnable!!)
    }

    private fun stopBlinkingEffect() {
        // Stop blinking effect
        blinkRunnable?.let { blinkHandler.removeCallbacks(it) }
        binding.yourBlinkingView.visibility = View.VISIBLE
    }

    private fun toggleFlashlight() {
        if (isFlashOn) {
            turnOffFlashlight()
        } else {
            turnOnFlashlight()
        }
    }

    private fun turnOnFlashlight() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                cameraId = cameraManager?.cameraIdList?.get(0) // Usually, the first camera has a flash
                cameraManager?.setTorchMode(cameraId!!, true)
                isFlashOn = true
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    private fun turnOffFlashlight() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                cameraManager?.setTorchMode(cameraId!!, false)
                isFlashOn = false
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, handle it accordingly
            } else {
                // Permission denied, handle it accordingly
            }
        }
    }
}
