package com.sendbird.uikit.samples.basic.openchannel.livestream

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sendbird.android.channel.OpenChannel
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.activities.adapter.OpenChannelListAdapter
import com.sendbird.uikit.activities.viewholder.BaseViewHolder
import com.sendbird.uikit.samples.R
import com.sendbird.uikit.samples.common.extensions.createOvalIcon
import com.sendbird.uikit.samples.common.extensions.isUsingDarkTheme
import com.sendbird.uikit.samples.common.extensions.setTextColorResource
import com.sendbird.uikit.samples.common.preferences.PreferenceUtils
import com.sendbird.uikit.samples.databinding.ViewLiveStreamListItemBinding
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

/**
 * RecyclerView adapter for `OpenChannel` list used for live stream.
 */
class LiveStreamListAdapter : OpenChannelListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<OpenChannel> {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ViewLiveStreamListItemBinding.inflate(inflater, parent, false)
        return LiveStreamingListViewHolder(binding)
    }

    private class LiveStreamingListViewHolder(private val binding: ViewLiveStreamListItemBinding) :
        BaseViewHolder<OpenChannel>(binding.root) {
        init {
            with(binding) {
                val isDark = PreferenceUtils.themeMode.isUsingDarkTheme()
                background.setBackgroundResource(if (isDark) R.drawable.selector_list_background_dark else R.drawable.selector_list_background_light)
                tvLiveTitle.setTextColorResource(if (isDark) R.color.ondark_text_high_emphasis else R.color.onlight_text_high_emphasis)
                tvCreator.setTextColorResource(if (isDark) R.color.ondark_text_mid_emphasis else R.color.onlight_text_mid_emphasis)
                tvBadge.setTextColorResource(if (isDark) R.color.ondark_text_mid_emphasis else R.color.onlight_text_mid_emphasis)
                tvBadge.setBackgroundResource(if (isDark) R.drawable.shape_live_badge_dark else R.drawable.shape_live_badge_light)
            }
        }

        override fun bind(openChannel: OpenChannel) {
            val count = openChannel.participantCount
            var text = count.toString()
            if (count > 1000) {
                text = String.format(Locale.US, "%.1fK", count / 1000f)
            }
            try {
                LiveStreamingChannelData(JSONObject(openChannel.data)).apply {
                    with(binding) {
                        tvParticipantCount.text = text
                        ivChannelThumbnail.visibility = View.VISIBLE
                        tvLiveTitle.visibility = View.VISIBLE
                        tvLiveTitle.text = name
                        tvCreator.visibility =
                            if (creator?.nickname?.isNotEmpty() == true) View.VISIBLE else View.GONE
                        tvCreator.text = creator?.nickname
                        tvBadge.visibility = if (tags[0].isEmpty()) View.GONE else View.VISIBLE
                        tvBadge.text = tags[0]
                        val context = root.context
                        Glide.with(context)
                            .load(liveUrl)
                            .override(ivLiveThumbnail.width, ivLiveThumbnail.height)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.color.background_600)
                            .into(ivLiveThumbnail)
                        val iconTint = if (SendbirdUIKit.isDarkMode()) R.color.onlight_text_high_emphasis else R.color.ondark_text_high_emphasis
                        val backgroundTint =
                            if (SendbirdUIKit.isDarkMode()) R.color.background_400 else R.color.background_300
                        val errorIcon = context.createOvalIcon(backgroundTint, R.drawable.icon_channels, iconTint)
                        Glide.with(context)
                            .load(thumbnailUrl)
                            .override(ivChannelThumbnail.width, ivChannelThumbnail.height)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(errorIcon)
                            .into(ivChannelThumbnail)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                binding.ivLiveThumbnail.setImageDrawable(null)
                binding.ivChannelThumbnail.visibility = View.GONE
                binding.tvLiveTitle.visibility = View.GONE
                binding.tvBadge.visibility = View.GONE
                binding.tvCreator.visibility = View.GONE
            }
        }
    }
}
