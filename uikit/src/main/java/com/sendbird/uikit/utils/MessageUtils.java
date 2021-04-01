package com.sendbird.uikit.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.User;
import com.sendbird.uikit.activities.viewholder.MessageType;
import com.sendbird.uikit.activities.viewholder.MessageViewHolderFactory;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.model.TimelineMessage;

public class MessageUtils {
    public static boolean isMine(@NonNull BaseMessage message) {
        if (message.getSender() == null) {
            return false;
        }
        return isMine(message.getSender().getUserId());
    }

    public static boolean isMine(@NonNull String senderId) {
        User currentUser = SendBird.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUserId().equals(senderId);
        }
        return false;
    }

    public static boolean isUnknownType(@NonNull BaseMessage message) {
        MessageType messageType = MessageViewHolderFactory.getMessageType(message);
        return messageType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_ME || messageType == MessageType.VIEW_TYPE_UNKNOWN_MESSAGE_OTHER;
    }

    public static boolean isGroupChanged(@Nullable BaseMessage frontMessage, @Nullable BaseMessage backMessage) {
        return frontMessage == null ||
                frontMessage.getSender() == null ||
                frontMessage instanceof AdminMessage ||
                frontMessage instanceof TimelineMessage ||
                backMessage == null ||
                backMessage.getSender() == null ||
                backMessage instanceof AdminMessage ||
                backMessage instanceof TimelineMessage ||
                !backMessage.getSendingStatus().equals(BaseMessage.SendingStatus.SUCCEEDED) ||
                !frontMessage.getSendingStatus().equals(BaseMessage.SendingStatus.SUCCEEDED) ||
                !frontMessage.getSender().equals(backMessage.getSender()) ||
                !DateUtils.hasSameTimeInMinute(frontMessage.getCreatedAt(), backMessage.getCreatedAt());
    }

    public static MessageGroupType getMessageGroupType(@Nullable BaseMessage prevMessage, @NonNull BaseMessage message, @Nullable BaseMessage nextMessage) {
        if (!message.getSendingStatus().equals(BaseMessage.SendingStatus.SUCCEEDED)) {
            return MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        MessageGroupType messageGroupType = MessageGroupType.GROUPING_TYPE_BODY;
        boolean isHead = MessageUtils.isGroupChanged(prevMessage, message);
        boolean isTail = MessageUtils.isGroupChanged(message, nextMessage);

        if (!isHead && isTail) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_TAIL;
        } else if (isHead && !isTail) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_HEAD;
        } else if (isHead) {
            messageGroupType = MessageGroupType.GROUPING_TYPE_SINGLE;
        }

        return messageGroupType;
    }
}
