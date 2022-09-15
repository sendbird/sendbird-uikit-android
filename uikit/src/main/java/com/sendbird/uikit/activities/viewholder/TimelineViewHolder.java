package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewTimeLineMessageBinding;
import com.sendbird.uikit.internal.ui.messages.TimelineMessageView;

import java.util.Map;

public final class TimelineViewHolder extends MessageViewHolder {
    @NonNull
    private final TimelineMessageView timelineMessageView;

    TimelineViewHolder(@NonNull SbViewTimeLineMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        timelineMessageView = binding.timelineMessageView;
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        timelineMessageView.setMessageUIConfig(messageUIConfig);
        timelineMessageView.drawTimeline(message);
    }

    @Override
    @NonNull
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
