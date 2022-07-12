package com.sendbird.uikit.customsample.openchannel.community;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.uikit.customsample.R;
import com.sendbird.uikit.customsample.databinding.ViewCommunityListItemBinding;
import com.sendbird.uikit.customsample.openchannel.OpenChannelListAdapter;
import com.sendbird.uikit.customsample.openchannel.OpenChannelListViewHolder;
import com.sendbird.uikit.customsample.utils.DrawableUtils;

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
            this.binding.ivFrozenIcon.setImageDrawable(DrawableUtils.setTintList(binding.getRoot().getContext(), R.drawable.icon_freeze, R.color.primary_300));
        }

        @Override
        protected void bind(OpenChannel openChannel) {
            if (openChannel == null) return;
            binding.tvCommunityTitle.setText(openChannel.getName());
            binding.ivFrozenIcon.setVisibility(openChannel.isFrozen() ? View.VISIBLE : View.GONE);

            Drawable errorIcon = DrawableUtils.createOvalIcon(binding.getRoot().getContext(),
                    R.color.background_300, R.drawable.icon_channels, R.color.ondark_01);
            Glide.with(binding.getRoot().getContext())
                    .load(openChannel.getCoverUrl())
                    .override(binding.ivCommunityCover.getWidth(), binding.ivCommunityCover.getHeight())
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(errorIcon)
                    .into(binding.ivCommunityCover);
        }
    }
}
