package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.model.HighlightMessageInfo;

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

    abstract public void drawMessage(GroupChannel channel, BaseMessage message, MessageGroupType messageGroupType);

    @BindingAdapter({"message", "channel", "messageGroupType"})
    public static void drawMessageWithChannel(GroupChannelMessageView view, BaseMessage message, GroupChannel channel, MessageGroupType messageGroupType) {
        drawMessageWithChannel(view, message, channel, messageGroupType, null);
    }

    @BindingAdapter({"message", "channel", "messageGroupType", "highlightInfo"})
    public static void drawMessageWithChannel(GroupChannelMessageView view, BaseMessage message, GroupChannel channel, MessageGroupType messageGroupType, HighlightMessageInfo highlightMessageInfo) {
        view.setHighlightMessageInfo(highlightMessageInfo);
        view.drawMessage(channel, message, messageGroupType);
    }
}
