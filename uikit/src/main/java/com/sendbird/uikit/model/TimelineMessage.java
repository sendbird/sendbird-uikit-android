package com.sendbird.uikit.model;

import androidx.annotation.NonNull;

import com.sendbird.android.message.BaseMessage;
import com.sendbird.android.message.CustomizableMessage;
import com.sendbird.uikit.utils.DateUtils;

public class TimelineMessage extends CustomizableMessage {
    @NonNull
    private final BaseMessage anchor;

    public TimelineMessage(@NonNull BaseMessage anchor) {
        super(anchor.getChannelUrl(), anchor.getMessageId() + anchor.getCreatedAt(), anchor.getCreatedAt() - 1);
        this.anchor = anchor;
    }

    @Override
    @NonNull
    public String getRequestId() {
        return anchor.getRequestId() + getCreatedAt();
    }

    @Override
    @NonNull
    public String getMessage() {
        return DateUtils.formatTimelineMessage(getCreatedAt());
    }
}
