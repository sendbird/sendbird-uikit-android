package com.sendbird.uikit_messaging_android.openchannel.livestream;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.uikit.SendbirdUIKit;
import com.sendbird.uikit.interfaces.UserInfo;
import com.sendbird.uikit.utils.TextUtils;
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.databinding.ViewLiveStreamListItemBinding;
import com.sendbird.uikit_messaging_android.model.LiveStreamingChannelData;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelListAdapter;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelListViewHolder;
import com.sendbird.uikit_messaging_android.utils.DrawableUtils;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * RecyclerView adapter for <code>OpenChannel</code> list used for live stream.
 */
public class LiveStreamListAdapter extends OpenChannelListAdapter<LiveStreamListAdapter.LiveStreamingListViewHolder> {
    @NonNull
    @Override
    public LiveStreamingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewLiveStreamListItemBinding binding = ViewLiveStreamListItemBinding.inflate(inflater, parent, false);
        return new LiveStreamingListViewHolder(binding);
    }

    static class LiveStreamingListViewHolder extends OpenChannelListViewHolder {
        private final ViewLiveStreamListItemBinding binding;

        public LiveStreamingListViewHolder(@NonNull ViewLiveStreamListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            Context context = binding.getRoot().getContext();
            boolean isDark = PreferenceUtils.isUsingDarkTheme();
            this.binding.background.setBackgroundResource(isDark ? R.drawable.selector_list_background_dark : R.drawable.selector_list_background_light);
            this.binding.tvLiveTitle.setTextColor(context.getResources().getColor(isDark ? R.color.ondark_01 : R.color.onlight_01));
            this.binding.tvCreator.setTextColor(context.getResources().getColor(isDark ? R.color.ondark_02 : R.color.onlight_02));
            this.binding.tvBadge.setTextColor(context.getResources().getColor(isDark ? R.color.ondark_02 : R.color.onlight_02));
            this.binding.tvBadge.setBackgroundResource(isDark ? R.drawable.shape_live_badge_dark : R.drawable.shape_live_badge_light);
        }

        @Override
        protected void bind(OpenChannel openChannel) {
            if (openChannel == null) return;
            int count = openChannel.getParticipantCount();
            String text = String.valueOf(count);
            if (count > 1000) {
                text = String.format(Locale.US, "%.1fK", count / 1000F);
            }
            binding.tvParticipantCount.setText(text);

            try {
                LiveStreamingChannelData channelData = new LiveStreamingChannelData(new JSONObject(openChannel.getData()));

                binding.tvLiveTitle.setVisibility(View.VISIBLE);
                binding.tvLiveTitle.setText(channelData.getName());

                UserInfo creatorInfo = channelData.getCreator();
                if (creatorInfo == null || TextUtils.isEmpty(creatorInfo.getNickname())) {
                    binding.tvCreator.setVisibility(View.GONE);
                } else {
                    binding.tvCreator.setVisibility(View.VISIBLE);
                    binding.tvCreator.setText(creatorInfo.getNickname());
                }

                if (channelData.getTags() == null || TextUtils.isEmpty(channelData.getTags().get(0))) {
                    binding.tvBadge.setVisibility(View.GONE);
                } else {
                    binding.tvBadge.setVisibility(View.VISIBLE);
                    binding.tvBadge.setText(channelData.getTags().get(0));
                }

                Context context = binding.getRoot().getContext();
                Glide.with(context)
                        .load(channelData.getLiveUrl())
                        .override(binding.ivLiveThumbnail.getWidth(), binding.ivLiveThumbnail.getHeight())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.color.background_600)
                        .into(binding.ivLiveThumbnail);

                binding.ivChannelThumbnail.setVisibility(View.VISIBLE);

                int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
                int backgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_400 : R.color.background_300;
                Drawable errorIcon = DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_channels, iconTint);
                Glide.with(context)
                        .load(channelData.getThumbnailUrl())
                        .override(binding.ivChannelThumbnail.getWidth(), binding.ivChannelThumbnail.getHeight())
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(errorIcon)
                        .into(binding.ivChannelThumbnail);
            } catch (JSONException e) {
                e.printStackTrace();
                binding.ivLiveThumbnail.setImageDrawable(null);
                binding.ivChannelThumbnail.setVisibility(View.GONE);
                binding.tvLiveTitle.setVisibility(View.GONE);
                binding.tvBadge.setVisibility(View.GONE);
                binding.tvCreator.setVisibility(View.GONE);
            }
        }
    }
}
