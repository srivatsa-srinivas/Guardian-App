package com.nikhil.guardian

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService

class VideoCallMain : AppCompatActivity() {

    private lateinit var userIdTextField: EditText
    private lateinit var button: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_main)

        userIdTextField = findViewById(R.id.user_id_text_field)
        button = findViewById(R.id.button_next)

        button.setOnClickListener {
            val userId = userIdTextField.text.toString()
            if (userId.isNotEmpty()) {
                val intent = Intent(this@VideoCallMain, VideoCallActivity::class.java)
                intent.putExtra("userID", userId)
                startActivity(intent)

                videoCallServices(userId)
            }
        }

    }

    private fun videoCallServices(userID: String) {
        val appID: Long = 1626032414 // your App ID of Zoge Cloud
        val appSign = "dde5b97e4d17446fefed5e38df53eb73afd02be6b2fa563f3e178aba18c7439b" // your App Sign of Zoge Cloud
        val application = application // Android's application context
        val callInvitationConfig = ZegoUIKitPrebuiltCallInvitationConfig()
        //callInvitationConfig.notifyWhenAppRunningInBackgroundOrQuit = true
        val notificationConfig = ZegoNotificationConfig()
        notificationConfig.sound = "zego_uikit_sound_call"
        notificationConfig.channelID = "CallInvitation"
        notificationConfig.channelName = "CallInvitation"
        ZegoUIKitPrebuiltCallInvitationService.init(
            application,
            appID,
            appSign,
            userID,
            userID,
            callInvitationConfig
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        ZegoUIKitPrebuiltCallInvitationService.unInit()
    }
}