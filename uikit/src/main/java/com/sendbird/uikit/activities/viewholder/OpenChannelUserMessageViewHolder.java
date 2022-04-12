package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelUserMessageBinding;
import com.sendbird.uikit.widgets.OpenChannelUserMessageView;

import java.util.Map;

public final class OpenChannelUserMessageViewHolder extends MessageViewHolder {
    @NonNull
    private final OpenChannelUserMessageView openChannelUserMessageView;

    OpenChannelUserMessageViewHolder(@NonNull SbViewOpenChannelUserMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        openChannelUserMessageView = binding.otherMessageView;
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), openChannelUserMessageView.getBinding().contentPanel);
        clickableViewMap.put(ClickableViewIdentifier.Profile.name(), openChannelUserMessageView.getBinding().ivProfileView);
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        if (channel instanceof OpenChannel) {
            openChannelUserMessageView.drawMessage((OpenChannel) channel, message, messageGroupType);
        }
    }

    @Override
    @NonNull
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
