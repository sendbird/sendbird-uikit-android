package com.sendbird.uikit.samples.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sendbird.android.SendbirdChat
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.activities.FeedNotificationChannelActivity
import com.sendbird.uikit.modules.components.HeaderComponent
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.extensions.setAppearance
import com.sendbird.uikit.samples.databinding.LayoutFeedChannelListBinding
import com.sendbird.uikit.samples.databinding.ViewFeedChannelItemBinding

class FeedChannelListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return getContentView(inflater, savedInstanceState)
    }

    private fun getContentView(inflater: LayoutInflater, savedInstanceState: Bundle?): View {
        val binding = LayoutFeedChannelListBinding.inflate(inflater)
        val themeMode = SendbirdUIKit.getDefaultThemeMode()
        binding.feedChannelList.setBackgroundResource(
            if (themeMode == SendbirdUIKit.ThemeMode.Light) R.color.background_50 else R.color.background_600
        )

        HeaderComponent().apply {
            this.params.title = getString(R.string.text_tab_channels)
            this.params.setUseLeftButton(false)
            this.params.setUseRightButton(false)
            val header = this.onCreateView(requireContext(), inflater, binding.headerComponent, savedInstanceState)
            binding.headerComponent.addView(header)
        }

        SendbirdChat.appInfo?.notificationInfo?.feedChannels?.let { feedChannels ->
            val background = if (themeMode == SendbirdUIKit.ThemeMode.Light) R.drawable.selector_rectangle_light else R.drawable.selector_rectangle_dark600
            val dividerColor = if (themeMode == SendbirdUIKit.ThemeMode.Light) R.color.onlight_text_disabled else R.color.ondark_text_disabled
            val channelKeyStyle = if (themeMode == SendbirdUIKit.ThemeMode.Light) R.style.SendbirdSubtitle1OnLight01 else R.style.SendbirdSubtitle1OnDark01
            val channelUrlStyle = if (themeMode == SendbirdUIKit.ThemeMode.Light) R.style.SendbirdBody3OnLight03 else R.style.SendbirdBody3OnDark03
            feedChannels.entries.forEach { entry ->
                ViewFeedChannelItemBinding.inflate(inflater).apply {
                    root.setBackgroundResource(background)
                    divider.setBackgroundResource(dividerColor)
                    tvChannelKey.text = entry.key
                    tvChannelUrl.text = entry.value
                    tvChannelKey.setAppearance(requireContext(), channelKeyStyle)
                    tvChannelUrl.setAppearance(requireContext(), channelUrlStyle)
                    root.setOnClickListener {
                        startActivity(
                            FeedNotificationChannelActivity.newIntent(
                                requireContext(),
                                entry.value
                            )
                        )
                    }
                    binding.feedChannelContainer.addView(root)
                }
            }
        }
        return binding.root
    }
}
