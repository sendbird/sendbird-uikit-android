package com.sendbird.uikit_messaging_android.openchannel.livestream

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.sendbird.android.channel.OpenChannel
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.activities.adapter.OpenChannelListAdapter
import com.sendbird.uikit.activities.viewholder.BaseViewHolder
import com.sendbird.uikit_messaging_android.R
import com.sendbird.uikit_messaging_android.databinding.ViewLiveStreamListItemBinding
import com.sendbird.uikit_messaging_android.model.LiveStreamingChannelData
import com.sendbird.uikit_messaging_android.utils.DrawableUtils
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils
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
            val context = binding.root.context
            val isDark = PreferenceUtils.isUsingDarkTheme
            binding.background.setBackgroundResource(if (isDark) R.drawable.selector_list_background_dark else R.drawable.selector_list_background_light)
            binding.tvLiveTitle.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    if (isDark) R.color.ondark_01 else R.color.onlight_01,
                    null
                )
            )
            binding.tvCreator.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    if (isDark) R.color.ondark_02 else R.color.onlight_02,
                    null
                )
            )
            binding.tvBadge.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    if (isDark) R.color.ondark_02 else R.color.onlight_02,
                    null
                )
            )
            binding.tvBadge.setBackgroundResource(
                if (isDark) R.drawable.shape_live_badge_dark else R.drawable.shape_live_badge_light
            )
        }

        override fun bind(openChannel: OpenChannel) {
            val count = openChannel.participantCount
            var text = count.toString()
            if (count > 1000) {
                text = String.format(Locale.US, "%.1fK", count / 1000f)
            }
            binding.tvParticipantCount.text = text
            try {
                val channelData = LiveStreamingChannelData(JSONObject(openChannel.data))
                binding.tvLiveTitle.visibility = View.VISIBLE
                binding.tvLiveTitle.text = channelData.name
                val creatorInfo = channelData.creator
                if (creatorInfo == null || creatorInfo.nickname.isEmpty()) {
                    binding.tvCreator.visibility = View.GONE
                } else {
                    binding.tvCreator.visibility = View.VISIBLE
                    binding.tvCreator.text = creatorInfo.nickname
                }
                val tags = channelData.tags
                if (tags[0].isEmpty()) {
                    binding.tvBadge.visibility = View.GONE
                } else {
                    binding.tvBadge.visibility = View.VISIBLE
                    binding.tvBadge.text = tags[0]
                }
                val context = binding.root.context
                Glide.with(context)
                    .load(channelData.liveUrl)
                    .override(binding.ivLiveThumbnail.width, binding.ivLiveThumbnail.height)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.color.background_600)
                    .into(binding.ivLiveThumbnail)
                binding.ivChannelThumbnail.visibility = View.VISIBLE
                val iconTint = if (SendbirdUIKit.isDarkMode()) R.color.onlight_01 else R.color.ondark_01
                val backgroundTint = if (SendbirdUIKit.isDarkMode()) R.color.background_400 else R.color.background_300
                val errorIcon =
                    DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_channels, iconTint)
                Glide.with(context)
                    .load(channelData.thumbnailUrl)
                    .override(binding.ivChannelThumbnail.width, binding.ivChannelThumbnail.height)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(errorIcon)
                    .into(binding.ivChannelThumbnail)
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
