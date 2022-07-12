package com.sendbird.uikit.customsample.groupchannel.components.viewholders;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.GroupChannel;
import com.sendbird.uikit.activities.viewholder.BaseViewHolder;
import com.sendbird.uikit.customsample.databinding.ViewCustomChannelHolderBinding;
import com.sendbird.uikit.utils.ChannelUtils;
import com.sendbird.uikit.utils.DateUtils;

/**
 * ViewHolder to draw the channel list item for the <code>GroupChannel</code>.
 */
public class CustomChannelViewHolder extends BaseViewHolder<GroupChannel> {
    private final ViewCustomChannelHolderBinding binding;

    public CustomChannelViewHolder(@NonNull ViewCustomChannelHolderBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @Override
    public void bind(@NonNull GroupChannel channel) {
        binding.tvTitle.setText(ChannelUtils.makeTitleText(binding.getRoot().getContext(), channel));

        if (channel.getLastMessage() != null) {
            binding.tvLastMessage.setText(channel.getLastMessage().getMessage());
            binding.tvUpdatedAt.setText(DateUtils.formatDateTime(binding.getRoot().getContext(), channel.getLastMessage().getCreatedAt()));
        }

        if (channel.getMemberCount() > 2) {
            binding.tvMemberCount.setVisibility(View.VISIBLE);
            binding.tvMemberCount.setText(String.valueOf(channel.getMemberCount()));
        } else {
            binding.tvMemberCount.setVisibility(View.GONE);
        }

        if (channel.getUnreadMessageCount() > 0) {
            binding.tvUnreadCount.setVisibility(View.VISIBLE);
            binding.tvUnreadCount.setText(String.valueOf(channel.getUnreadMessageCount()));
        } else {
            binding.tvUnreadCount.setVisibility(View.GONE);
        }

        if (channel.getMyPushTriggerOption() == GroupChannel.PushTriggerOption.OFF) {
            binding.ivPushEnabledIcon.setVisibility(View.VISIBLE);
        } else {
            binding.ivPushEnabledIcon.setVisibility(View.GONE);
        }
    }
}
