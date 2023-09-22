package com.sendbird.uikit_messaging_android

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat.sdkVersion
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.user.User
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit_messaging_android.databinding.ActivityLoginBinding
import com.sendbird.uikit_messaging_android.fcm.MyFirebaseMessagingService
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils
import com.sendbird.uikit_messaging_android.utils.PushUtils
import com.sendbird.uikit_messaging_android.widgets.WaitingDialog

/**
 * Displays a login screen.
 */
class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)
        if (BuildConfig.DEBUG) {
            binding.applicationId.visibility = View.VISIBLE
            binding.btSave.visibility = View.VISIBLE
            if (SendbirdUIKit.getAdapter() != null) {
                binding.applicationId.setText(SendbirdUIKit.getAdapter()!!.appId)
            }
            binding.btSave.setOnClickListener { _: View? ->
                val appId = binding.applicationId.text
                if (!appId.isNullOrEmpty()) {
                    PreferenceUtils.appId = appId.toString()
                    finish()
                    Process.killProcess(Process.myPid())
                }
            }
        } else {
            binding.applicationId.visibility = View.GONE
            binding.btSave.visibility = View.GONE
        }
        binding.etUserId.setSelectAllOnFocus(true)
        binding.etNickname.setSelectAllOnFocus(true)
        val sdkVersion = String.format(
            resources.getString(R.string.text_version_info),
            com.sendbird.uikit.BuildConfig.VERSION_NAME,
            sdkVersion
        )
        binding.tvVersionInfo.text = sdkVersion
        binding.btSignIn.setOnClickListener { _: View? ->
            val userId = binding.etUserId.text
            val userNickname = binding.etNickname.text
            if (userId.isNullOrEmpty() || userNickname.isNullOrEmpty()) {
                return@setOnClickListener
            }

            // Remove all spaces from userID
            val userIdString = userId.toString().replace("\\s".toRegex(), "")
            PreferenceUtils.userId = userIdString
            PreferenceUtils.nickname = userNickname.toString()
            WaitingDialog.show(this)
            SendbirdUIKit.connect { _: User?, e: SendbirdException? ->
                if (e != null) {
                    Logger.e(e)
                    WaitingDialog.dismiss()
                    PreferenceUtils.clearAll()
                    return@connect
                }
                WaitingDialog.dismiss()
                PreferenceUtils.userId = userIdString
                PreferenceUtils.nickname = userNickname.toString()
                PushUtils.registerPushHandler(MyFirebaseMessagingService())
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
