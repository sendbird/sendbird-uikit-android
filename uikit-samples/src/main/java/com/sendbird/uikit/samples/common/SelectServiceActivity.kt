package com.sendbird.uikit.samples.common

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sendbird.android.SendbirdChat
import com.sendbird.uikit.BuildConfig
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.log.Logger
import com.sendbird.uikit.samples.BaseApplication
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.aichatbot.AiChatBotLoginActivity
import com.sendbird.uikit.samples.common.consts.InitState
import com.sendbird.uikit.samples.common.consts.Region
import com.sendbird.uikit.samples.common.consts.SampleType
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.common.widgets.WaitingDialog
import com.sendbird.uikit.samples.databinding.ActivitySelectServiceBinding
import com.sendbird.uikit.samples.enableAiChatBotSample
import com.sendbird.uikit.samples.enableNotificationSample
import com.sendbird.uikit.samples.notification.NotificationLoginActivity
import java.util.regex.Pattern

private const val APP_ID_FORMAT_REX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
class SelectServiceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySelectServiceBinding.inflate(layoutInflater).apply {
            with(applicationId) {
                setSelectAllOnFocus(false)
                SendbirdUIKit.getAdapter()?.appId?.let { setText(it) }

                val appId = text
                if (!appId.isNullOrEmpty()) {
                    PreferenceUtils.appId = appId.toString()
                }
            }
            versionInfo.text = String.format(
                resources.getString(R.string.text_version_info),
                BuildConfig.VERSION_NAME,
                SendbirdChat.sdkVersion
            )

            regionSpinner.setSelection(PreferenceUtils.region.ordinal)
            regionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val selectedRegion = parent.getItemAtPosition(position).toString()
                    Logger.d("Selected region: $selectedRegion")
                    PreferenceUtils.region = Region.valueOf(selectedRegion)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            basicSampleButton.setOnClickListener {
                changeServiceType(
                    SampleType.Basic,
                    applicationId.text?.toString(),
                    Intent(this@SelectServiceActivity, LoginActivity::class.java)
                )
            }
            aiChatBotSampleButton.setOnClickListener {
                if (enableAiChatBotSample) {
                    changeServiceType(
                        SampleType.AiChatBot,
                        applicationId.text?.toString(),
                        Intent(this@SelectServiceActivity, AiChatBotLoginActivity::class.java)
                    )
                } else {
                    showSampleNotSupported("https://github.com/sendbird/sendbird-uikit-android/tree/main/uikit-samples/src/main/java/com/sendbird/uikit/samples/aichatbot/README.md")
                }
            }
            customizationSampleButton.setOnClickListener {
                changeServiceType(
                    SampleType.Customization,
                    applicationId.text?.toString(),
                    Intent(this@SelectServiceActivity, LoginActivity::class.java)
                )
            }
            notificationSampleButton.setOnClickListener {
                if (enableNotificationSample) {
                    changeServiceType(
                        SampleType.Notification,
                        applicationId.text?.toString(),
                        Intent(this@SelectServiceActivity, NotificationLoginActivity::class.java)
                    )
                } else {
                    showSampleNotSupported("https://github.com/sendbird/sendbird-uikit-android/tree/main/uikit-samples/src/main/java/com/sendbird/uikit/samples/notification/README.md")
                }
            }
        }
        setContentView(binding.root)
    }

    private fun changeServiceType(type: SampleType, appId: String?, intent: Intent) {
        if (!isValidAppIdFormat(appId)) {
            Toast.makeText(this, "This AppID is not allowed format", Toast.LENGTH_SHORT).show()
            return
        }
        PreferenceUtils.selectedSampleType = type
        val changed = saveAppId(appId)
        if (!changed) {
            startActivity(intent)
            finish()
            return
        }

        SendbirdUIKit.initFromForeground(BaseApplication.adapter, this)
        BaseApplication.initStateChanges().observe(this) {
            when (it) {
                null, InitState.NONE -> {}
                InitState.MIGRATING -> WaitingDialog.show(this)
                InitState.FAILED, InitState.SUCCEED -> {
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    /**
     * Save app id.
     *
     * @param appId the app id
     * @return true if the app id is changed, false otherwise
     */
    private fun saveAppId(appId: String?): Boolean {
        val prevAppId = PreferenceUtils.appId
        if (prevAppId != appId && !appId.isNullOrEmpty()) {
            PreferenceUtils.appId = appId.toString()
            return true
        }
        return false
    }

    private fun isValidAppIdFormat(appId: String?): Boolean {
        return appId?.let {
            val pattern = Pattern.compile(APP_ID_FORMAT_REX)
            val matcher = pattern.matcher(it)
            return matcher.matches()
        } ?: false
    }

    private fun showSampleNotSupported(url: String) {
        val builder = AlertDialog.Builder(this@SelectServiceActivity)
        builder.setTitle(getString(R.string.sb_text_dialog_not_supported_title))
        builder.setMessage(getString(R.string.text_not_supported))
        builder.setPositiveButton(R.string.sb_text_go_to_readme) { _: DialogInterface?, _: Int ->
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(ContextCompat.getColor(this@SelectServiceActivity, com.sendbird.uikit.R.color.secondary_main))
    }
}
