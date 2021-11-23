package com.sendbird.uikit.activities.adapter;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.utils.MessageUtils;

import java.util.List;

class OpenChannelMessageDiffCallback extends DiffUtil.Callback {
    private final List<BaseMessage> oldMessageList;
    private final List<BaseMessage> newMessageList;
    private final OpenChannel oldChannel;
    private final OpenChannel newChannel;
    private final boolean useMessageGroupUI;

    public OpenChannelMessageDiffCallback(@NonNull OpenChannel oldChannel, @NonNull OpenChannel newChannel, @NonNull List<BaseMessage> oldMessageList, @NonNull List<BaseMessage> newMessageList, boolean useMessageGroupUI) {
        this.oldChannel = oldChannel;
        this.newChannel = newChannel;
        this.oldMessageList = oldMessageList;
        this.newMessageList = newMessageList;
        this.useMessageGroupUI = useMessageGroupUI;
    }

    @Override
    public int getOldListSize() {
        return oldMessageList.size();
    }

    @Override
    public int getNewListSize() {
        return newMessageList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        BaseMessage oldMessage = oldMessageList.get(oldItemPosition);
        BaseMessage newMessage = newMessageList.get(newItemPosition);
        String oldItemId = getItemId(oldMessage);
        String newItemId = getItemId(newMessage);
        return oldItemId.equals(newItemId);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        BaseMessage oldMessage = oldMessageList.get(oldItemPosition);
        BaseMessage newMessage = newMessageList.get(newItemPosition);

        if (!areItemsTheSame(oldItemPosition, newItemPosition)) {
            return false;
        }

        if (oldMessage.getSendingStatus() != newMessage.getSendingStatus()) {
            return false;
        }

        if (oldMessage.getUpdatedAt() != newMessage.getUpdatedAt()) {
            return false;
        }

        if (oldChannel.isFrozen() != newChannel.isFrozen()) {
            return false;
        }

        if (oldMessage.getOgMetaData() == null && newMessage.getOgMetaData() != null) {
            return false;
        } else if (oldMessage.getOgMetaData() != null && !oldMessage.getOgMetaData().equals(newMessage.getOgMetaData())) {
            return false;
        }

        if (useMessageGroupUI) {
            BaseMessage oldPrevMessage = oldItemPosition - 1 < 0 ? null : oldMessageList.get(oldItemPosition - 1);
            BaseMessage newPrevMessage = newItemPosition - 1 < 0 ? null : newMessageList.get(newItemPosition - 1);
            BaseMessage oldNextMessage = oldItemPosition + 1 >= oldMessageList.size() ? null : oldMessageList.get(oldItemPosition + 1);
            BaseMessage newNextMessage = newItemPosition + 1 >= newMessageList.size() ? null : newMessageList.get(newItemPosition + 1);
            MessageGroupType oldMessageGroupType = MessageUtils.getMessageGroupType(oldPrevMessage, oldMessage, oldNextMessage);
            MessageGroupType newMessageGroupType = MessageUtils.getMessageGroupType(newPrevMessage, newMessage, newNextMessage);

            if (oldMessageGroupType != newMessageGroupType) {
                return false;
            }
        }

        return true;
    }
    private String getItemId(@NonNull BaseMessage item) {
        if (TextUtils.isEmpty(item.getRequestId())) {
            return String.valueOf(item.getMessageId());
        } else {
            try {
                return item.getRequestId();
            } catch (Exception e) {
                return String.valueOf(item.getMessageId());
            }
        }
    }

}
