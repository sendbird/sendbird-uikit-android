package com.sendbird.uikit.activities.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.GroupChannel;

import java.util.List;

class ChannelDiffCallback extends DiffUtil.Callback {
    private final List<ChannelListAdapter.ChannelInfo> oldChannelList;
    private final List<GroupChannel> newChannelList;

    ChannelDiffCallback(@NonNull List<ChannelListAdapter.ChannelInfo> oldChannelList, @NonNull List<GroupChannel> newChannelList) {
        this.oldChannelList = oldChannelList;
        this.newChannelList = newChannelList;
    }

    @Override
    public int getOldListSize() {
        return oldChannelList.size();
    }

    @Override
    public int getNewListSize() {
        return newChannelList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        ChannelListAdapter.ChannelInfo oldChannel = oldChannelList.get(oldItemPosition);
        GroupChannel newChannel = newChannelList.get(newItemPosition);
        if (!newChannel.getUrl().equals(oldChannel.getChannelUrl())) {
            return false;
        }

        return newChannel.getCreatedAt() == oldChannel.getCreatedAt();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        ChannelListAdapter.ChannelInfo oldChannel = oldChannelList.get(oldItemPosition);
        GroupChannel newChannel = newChannelList.get(newItemPosition);

        if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
            return false;
        }

        String lastMessage = oldChannel.getLastMessage();
        String newLastMessage = newChannel.getLastMessage() != null ? newChannel.getLastMessage().getMessage() : "";
        if (!newLastMessage.equals(lastMessage)) {
            return false;
        }

        if (oldChannel.getPushTriggerOption() != newChannel.getMyPushTriggerOption()) {
            return false;
        }

        if (oldChannel.getUnreadMessageCount() != newChannel.getUnreadMessageCount()) {
            return false;
        }

        if (oldChannel.getMemberCount() != newChannel.getMemberCount()) {
            return false;
        }

        if (oldChannel.isFrozen() != newChannel.isFrozen()) {
            return false;
        }

        if (oldChannel.getCoverImageHash() != ChannelListAdapter.ChannelInfo.toUrlsHash(newChannel)) {
            return false;
        }

        String channelName = oldChannel.getChannelName() != null ? oldChannel.getChannelName() : "";
        if (!channelName.equals(newChannel.getName())) {
            return false;
        }

        String coverUrl = oldChannel.getCoverImageUrl() != null ? oldChannel.getCoverImageUrl() : "";
        return coverUrl.equals(newChannel.getCoverUrl());
    }
}
