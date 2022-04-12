package com.sendbird.uikit.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.Sender;
import com.sendbird.uikit.utils.DateUtils;

public class TimelineMessage extends BaseMessage {
    @NonNull
    private final BaseMessage anchor;

    public TimelineMessage(@NonNull BaseMessage anchor) {
        super(anchor.getChannelUrl(), anchor.getMessageId() + anchor.getCreatedAt(), anchor.getCreatedAt() - 1);
        this.anchor = anchor;
    }

    @Override
    @NonNull
    public String getRequestId() {
        return anchor.getRequestId() + mCreatedAt;
    }

    @Override
    @NonNull
    public String getMessage() {
        return DateUtils.formatDate(mCreatedAt);
    }

    @Override
    @Nullable
    public Sender getSender() {
        return null;
    }
}
