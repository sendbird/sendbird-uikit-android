package com.sendbird.uikit.samples.common

import android.content.Intent
import android.os.Bundle
import com.sendbird.android.SendbirdChat.sdkVersion
import com.sendbird.android.exception.SendbirdError
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.push.SendbirdPushHelper
import com.sendbird.android.user.User
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.extensions.apiHost
import com.sendbird.uikit.samples.common.extensions.authenticate
import com.sendbird.uikit.samples.common.extensions.getLogoDrawable
import com.sendbird.uikit.samples.common.extensions.startingIntent
import com.sendbird.uikit.samples.common.extensions.wsHost
import com.sendbird.uikit.samples.common.fcm.MyFirebaseMessagingService
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.samples.databinding.ActivityLoginBinding
import com.sendbird.uikit.utils.ContextUtils

/**
 * Displays a login screen.
 */
open class LoginActivity : BaseActivity() {
    protected val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    protected val inputUserId: String
        get() = binding.userId.text.toString().replace("\\s".toRegex(), "")
    protected val inputNickname: String
        get() = binding.nickname.text.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            val context = this@LoginActivity
            userId.setSelectAllOnFocus(true)
            nickname.setSelectAllOnFocus(true)
            versionInfo.text = String.format(
                resources.getString(R.string.text_version_info),
                com.sendbird.uikit.BuildConfig.VERSION_NAME,
                sdkVersion
            )

            val sampleType = PreferenceUtils.selectedSampleType
            logoImageView.background = sampleType.getLogoDrawable(context)
            title.text = "${sampleType?.name} Sample"
            signInButton.setOnClickListener {
                // Remove all spaces from userID
                if (inputUserId.isEmpty() || inputNickname.isEmpty()) {
                    return@setOnClickListener
                }
                PreferenceUtils.userId = inputUserId
                PreferenceUtils.nickname = inputNickname
                // set region
                val region = PreferenceUtils.region
                SendbirdUIKit.setCustomHosts(
                    region.apiHost(),
                    region.wsHost()
                )
                onSignUp()
            }
            selectSampleLayout.setOnClickListener {
                startActivity(Intent(context, SelectServiceActivity::class.java))
                finish()
            }
        }
        setContentView(binding.root)
    }

    open fun onSignUp() {
        Logger.i(">> LoginActivity::onSignUp()")
        WaitingDialog.show(this)
        authenticate { _: User?, e: SendbirdException? ->
            WaitingDialog.dismiss()
            if (e != null) {
                Logger.e(e)
                PreferenceUtils.userId = ""
                PreferenceUtils.nickname = ""

                ContextUtils.toastError(this@LoginActivity, "${e.message}")
                return@authenticate
            }

            onSignIn()
        }
    }

    protected fun onSignIn() {
        Logger.i(">> LoginActivity::onSignIn()")
        PreferenceUtils.userId = inputUserId
        PreferenceUtils.nickname = inputNickname

        SendbirdPushHelper.registerHandler(MyFirebaseMessagingService())
        val intent = PreferenceUtils.selectedSampleType.startingIntent(this)
        startActivity(intent)
        finish()
    }

    override fun onConnectedAfterDelay() {
        super.onConnectedAfterDelay()
        onSignIn()
    }
}
