package com.sendbird.uikit.samples.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.BaseApplication.Companion.initStateChanges
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.consts.InitState
import com.sendbird.uikit.samples.common.extensions.authenticate
import com.sendbird.uikit.samples.common.extensions.isUsingDarkTheme
import com.sendbird.uikit.samples.common.extensions.startingIntent
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.samples.databinding.ActivitySplashBinding
import com.sendbird.uikit.utils.ContextUtils

/**
 * Displays a splash screen.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivitySplashBinding.inflate(layoutInflater).apply {
            setContentView(root)
            val isDarkTheme = PreferenceUtils.themeMode.isUsingDarkTheme()
            val backgroundRedId = if (isDarkTheme) R.color.background_600 else R.color.background_50
            root.setBackgroundResource(backgroundRedId)
        }
        initStateChanges().observe(this) { initState: InitState ->
            Logger.i("++ init state : %s", initState)
            WaitingDialog.dismiss()
            when (initState) {
                InitState.NONE -> {}
                InitState.MIGRATING -> WaitingDialog.show(this@SplashActivity)
                InitState.FAILED, InitState.SUCCEED -> {
                    WaitingDialog.dismiss()
                    if (PreferenceUtils.userId.isNotEmpty()) {
                        authenticate { _, e ->
                            if (e != null) {
                                ContextUtils.toastError(this@SplashActivity, "${e.message}")
                                return@authenticate
                            }

                            moveToNext()
                        }
                    } else {
                        moveToNext()
                    }
                }
            }
        }
    }

    private fun getNextIntent(): Intent {
        return PreferenceUtils.selectedSampleType.startingIntent(this@SplashActivity)
    }

    private fun moveToNext() {
        startActivity(getNextIntent())
        finish()
    }

    override fun onConnectedAfterDelay() {
        super.onConnectedAfterDelay()

        moveToNext()
    }
}
