package com.nikhil.guardian

import android.content.Context
import android.graphics.Color
import android.hardware.camera2.CameraManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.nikhil.guardian.databinding.SosActivityLayoutBinding

class SosActivity : AppCompatActivity() {
    lateinit var binding: SosActivityLayoutBinding

    private var torchState: Boolean = false
    private lateinit var cameraManager: CameraManager
    private var camId: String = "0"
    private lateinit var handler: Handler
    private var blinking = false
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SosActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        camId = cameraManager.cameraIdList[0]

        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound)

        Dexter.withContext(this).withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    turnOnFlashLight()
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@SosActivity, "Please grant the permission", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }

            }).check()
    }

    private fun turnOnFlashLight() {
        binding.imgTorch.setOnClickListener {
            torchState = when (torchState) {
                false -> {
                    cameraManager.setTorchMode(camId, true)
                    binding.imgTorch.setImageResource(R.drawable.flashlight_on)
                    startBlinking()
                    playAlertSound()
                    true
                }
                true -> {
                    cameraManager.setTorchMode(camId, false)
                    binding.imgTorch.setImageResource(R.drawable.flashlight_off)
                    stopBlinking()
                    // Set the background color to default (white)
                    binding.root.setBackgroundColor(Color.WHITE)
                    stopAlertSound() // Stop the alert sound
                    false
                }
            }
        }
    }

    private fun startBlinking() {
        handler.post(object : Runnable {
            override fun run() {
                if (blinking) {
                    binding.root.setBackgroundColor(Color.RED)
                } else {
                    binding.root.setBackgroundColor(Color.WHITE)
                }

                blinking = !blinking

                handler.postDelayed(this, 500) // Adjust the delay as needed
            }
        })
    }

    private fun stopBlinking() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun playAlertSound() {
        mediaPlayer.start()
    }

    private fun stopAlertSound() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound) // Re-create the MediaPlayer
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
