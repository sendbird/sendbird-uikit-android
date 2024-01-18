package com.sendbird.uikit.samples.common

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.SendbirdChat.sdkVersion
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.push.SendbirdPushHelper
import com.sendbird.android.user.User
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.extensions.authenticate
import com.sendbird.uikit.samples.common.extensions.getLogoDrawable
import com.sendbird.uikit.samples.common.extensions.startingIntent
import com.sendbird.uikit.samples.common.fcm.MyFirebaseMessagingService
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.samples.databinding.ActivityLoginBinding
import com.sendbird.uikit.utils.ContextUtils

/**
 * Displays a login screen.
 */
open class LoginActivity : AppCompatActivity() {
    protected val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            val context = this@LoginActivity
            applicationId.visibility = View.VISIBLE
            saveButton.visibility = View.VISIBLE
            userId.setSelectAllOnFocus(true)
            nickname.setSelectAllOnFocus(true)
            versionInfo.text = String.format(
                resources.getString(R.string.text_version_info),
                com.sendbird.uikit.BuildConfig.VERSION_NAME,
                sdkVersion
            )
            SendbirdUIKit.getAdapter()?.appId?.let { applicationId.setText(it) }

            val sampleType = PreferenceUtils.selectedSampleType
            logoImageView.background = sampleType.getLogoDrawable(context)
            title.text = "${sampleType?.name} Sample"
            saveButton.setOnClickListener {
                val appId = applicationId.text
                if (!appId.isNullOrEmpty()) {
                    PreferenceUtils.appId = appId.toString()
                    saveButton.postDelayed({
                        finish()
                        Process.killProcess(Process.myPid())
                    }, 500)
                }
            }
            signInButton.setOnClickListener {
                // Remove all spaces from userID
                val userId = binding.userId.text.toString().replace("\\s".toRegex(), "")
                val nickname = binding.nickname.text.toString()
                if (userId.isEmpty() || nickname.isEmpty()) {
                    return@setOnClickListener
                }
                PreferenceUtils.userId = userId
                PreferenceUtils.nickname = nickname
                onSignUp(userId, nickname)
            }
            selectSampleLayout.setOnClickListener {
                PreferenceUtils.clearAll()
                startActivity(Intent(context, SelectServiceActivity::class.java))
                finish()
            }
        }
        setContentView(binding.root)
    }

    open fun onSignUp(userId: String, nickname: String) {
        Logger.i(">> LoginActivity::onSignUp()")
        WaitingDialog.show(this)
        authenticate { _: User?, e: SendbirdException? ->
            if (e != null) {
                Logger.e(e)
                ContextUtils.toastError(this@LoginActivity, "${e.message}")
                WaitingDialog.dismiss()
                return@authenticate
            }
            WaitingDialog.dismiss()
            PreferenceUtils.userId = userId
            PreferenceUtils.nickname = nickname
            SendbirdPushHelper.registerPushHandler(MyFirebaseMessagingService())
            val intent = PreferenceUtils.selectedSampleType.startingIntent(this)
            startActivity(intent)
            finish()
        }
    }
}
