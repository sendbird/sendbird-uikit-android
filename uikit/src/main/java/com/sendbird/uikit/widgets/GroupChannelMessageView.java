package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.consts.MessageGroupType;

public abstract class GroupChannelMessageView extends BaseMessageView {
    public GroupChannelMessageView(@NonNull Context context) {
        super(context);
    }

    public GroupChannelMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GroupChannelMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    abstract public void drawMessage(@NonNull GroupChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType);
}
