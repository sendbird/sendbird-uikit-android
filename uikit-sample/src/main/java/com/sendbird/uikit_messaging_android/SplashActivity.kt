package com.sendbird.uikit_messaging_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.user.User
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit_messaging_android.BaseApplication.Companion.initStateChanges
import com.sendbird.uikit_messaging_android.consts.InitState
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils
import com.sendbird.uikit_messaging_android.widgets.WaitingDialog

/**
 * Displays a splash screen.
 */
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initStateChanges().observe(this) { initState: InitState ->
            Logger.i("++ init state : %s", initState)
            WaitingDialog.dismiss()
            when (initState) {
                InitState.MIGRATING -> WaitingDialog.show(this@SplashActivity)
                InitState.FAILED, InitState.SUCCEED -> {
                    WaitingDialog.dismiss()
                    val userId = PreferenceUtils.userId
                    if (userId.isNotEmpty()) {
                        SendbirdUIKit.connect { _: User?, _: SendbirdException? ->
                            startActivity(getNextIntent())
                            finish()
                        }
                    } else {
                        startActivity(getNextIntent())
                        finish()
                    }
                }
            }
        }
    }

    private fun getNextIntent(): Intent {
        val userId = PreferenceUtils.userId
        return if (userId.isNotEmpty()) {
            Intent(this@SplashActivity, HomeActivity::class.java)
        } else Intent(
            this@SplashActivity,
            LoginActivity::class.java
        )
    }
}
