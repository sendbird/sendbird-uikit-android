package com.sendbird.uikit_messaging_android.openchannel.community;

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
import com.sendbird.uikit_messaging_android.R;
import com.sendbird.uikit_messaging_android.databinding.ViewCommunityListItemBinding;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelListAdapter;
import com.sendbird.uikit_messaging_android.openchannel.OpenChannelListViewHolder;
import com.sendbird.uikit_messaging_android.utils.DrawableUtils;
import com.sendbird.uikit_messaging_android.utils.PreferenceUtils;

/**
 * RecyclerView adapter for <code>OpenChannel</code> list used for community.
 */
public class CommunityListAdapter extends OpenChannelListAdapter<CommunityListAdapter.CommunityListViewHolder> {
    @NonNull
    @Override
    public CommunityListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewCommunityListItemBinding binding = ViewCommunityListItemBinding.inflate(inflater, parent, false);
        return new CommunityListViewHolder(binding);
    }

    static class CommunityListViewHolder extends OpenChannelListViewHolder {
        private final ViewCommunityListItemBinding binding;

        public CommunityListViewHolder(@NonNull ViewCommunityListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            Context context = binding.getRoot().getContext();
            boolean isDark = PreferenceUtils.isUsingDarkTheme();
            binding.background.setBackgroundResource(isDark ? R.drawable.selector_list_background_dark : R.drawable.selector_list_background_light);
            binding.tvCommunityTitle.setTextColor(context.getResources().getColor(isDark ? R.color.ondark_01 : R.color.onlight_01));
            binding.border.setBackgroundResource(isDark ? R.color.ondark_04 : R.color.onlight_04);
            int iconTint = isDark ? R.color.primary_200 : R.color.primary_300;
            this.binding.ivFrozenIcon.setImageDrawable(DrawableUtils.setTintList(binding.getRoot().getContext(), R.drawable.icon_freeze, iconTint));
        }

        @Override
        protected void bind(OpenChannel openChannel) {
            if (openChannel == null) return;
            binding.tvCommunityTitle.setText(openChannel.getName());
            binding.ivFrozenIcon.setVisibility(openChannel.isFrozen() ? View.VISIBLE : View.GONE);

            Context context = binding.getRoot().getContext();
            int iconTint = SendbirdUIKit.isDarkMode() ? R.color.onlight_01 : R.color.ondark_01;
            int backgroundTint = SendbirdUIKit.isDarkMode() ? R.color.background_400 : R.color.background_300;
            Drawable errorIcon = DrawableUtils.createOvalIcon(context, backgroundTint, R.drawable.icon_channels, iconTint);
            Glide.with(context)
                    .load(openChannel.getCoverUrl())
                    .override(binding.ivCommunityCover.getWidth(), binding.ivCommunityCover.getHeight())
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(errorIcon)
                    .into(binding.ivCommunityCover);
        }
    }
}
