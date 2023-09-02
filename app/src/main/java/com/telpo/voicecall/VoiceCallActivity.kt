package com.telpo.voicecall

import android.Manifest
import android.content.pm.PackageManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.telpo.voicecall.databinding.ActivityVoiceCallBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig


class VoiceCallActivity : AppCompatActivity() {
    private var ringtone: Ringtone? = null
    private var countdownTimer: CountDownTimer? = null
    private var localUid = 0
    private var isCallAccepted = false // Track whether the call is accepted

    private val appId: String by lazy {
        getString(R.string.agora_appid)
    }

    private val channelName: String by lazy {
        getString(R.string.agora_channel)
    }

    private val token: String by lazy {
        getString(R.string.agora_token)
    }
    private var isJoined = false

    private var agoraEngine: RtcEngine? = null
    private lateinit var binding: ActivityVoiceCallBinding // Data binding object

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    private fun checkSelfPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
    }

    private fun showMessage(message: String) {
        runOnUiThread {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize data binding
        binding = ActivityVoiceCallBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }

        setupVoiceSDKEngine()

        // Set up access to the UI elements using the binding
        binding.joinLeaveButton.setOnClickListener { joinLeaveChannel(it) }
    }

    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d("AgoraEvent", "onUserJoined: $uid")
            runOnUiThread {
                if (uid == localUid) {
                    binding.callScreen.visibility = View.VISIBLE
                } else {
                    binding.callScreen.visibility = View.GONE
                    playRingtone()
                }
                countdownTimer = object : CountDownTimer(10000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        // This method is called every second during the countdown
                        Log.e("Timer", "1 Second Passed")
                    }
                    override fun onFinish() {
                        // This method is called when the countdown finishes
                        stopRingtone()
                        binding.roomName.text = channelName
                        binding.infoText.text = "Remote user joined: $uid"
                        showMessage("Remote user joined: $uid")
                    }
                }
                countdownTimer?.start()
            }
        }
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.d("AgoraEvent", "onJoinChannelSuccess: $channel      $uid")
            runOnUiThread {
                isJoined = true
                showMessage("Joined Channel $channel")
                binding.infoText.text = "Waiting for a remote user to join"
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d("AgoraEvent", "Remote user offline $uid $reason")
            runOnUiThread {
                showMessage("Remote user offline $uid $reason")
                if (isJoined) binding.infoText.text = "Waiting for a remote user to join"
            }
        }

        override fun onLeaveChannel(stats: RtcStats) {
            runOnUiThread {
                binding.infoText.text = "Press the button to join a channel"
                isJoined = false
                countdownTimer?.start()
            }
        }
    }
    private fun setupVoiceSDKEngine() {
        Log.e("Agoraaa", "$appId   $token    $channelName ")
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appId
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            Log.d("AgoraEvent", "Agora engine created successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage("Error in setupVoiceSDKEngine: ${e.message}")
            Log.e("AgoraEvent", "Error in setupVoiceSDKEngine: ${e.message}")
        }
    }

    private fun joinChannel() {
        localUid = generateRandomUid()
        val options = ChannelMediaOptions()
        options.autoSubscribeAudio = true
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        agoraEngine?.joinChannel(token, channelName, localUid, options)
    }

    fun joinLeaveChannel(view: View) {
        if (isJoined) {
            agoraEngine?.leaveChannel()
            binding.joinLeaveButton.text = "Join"
            binding.callScreen.visibility = View.GONE
        } else {
            joinChannel()
            binding.joinLeaveButton.text = "Leave"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine?.leaveChannel()
        RtcEngine.destroy()
        countdownTimer?.cancel()
    }
    fun generateRandomUid(): Int {
        // Generate a random unique UID
        val minUid = 10000
        val maxUid = 99999
        return (minUid..maxUid).random()
    }

    private fun playRingtone() {
        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        ringtone?.play()
    }

    private fun stopRingtone() {
        ringtone?.stop()
    }
}
