package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileVideoMessageBinding;
import com.sendbird.uikit.internal.ui.messages.OpenChannelVideoFileMessageView;

import java.util.Map;

public final class OpenChannelVideoFileMessageViewHolder extends MessageViewHolder {
    @NonNull
    private final OpenChannelVideoFileMessageView openChannelVideoFileMessageView;

    OpenChannelVideoFileMessageViewHolder(@NonNull SbViewOpenChannelFileVideoMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        openChannelVideoFileMessageView = binding.openChannelVideoFileMessageView;
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), openChannelVideoFileMessageView.getBinding().ivThumbnailOveray);
        clickableViewMap.put(ClickableViewIdentifier.Profile.name(), openChannelVideoFileMessageView.getBinding().ivProfileView);
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        openChannelVideoFileMessageView.setMessageUIConfig(messageUIConfig);
        if (channel instanceof OpenChannel) {
            openChannelVideoFileMessageView.drawMessage((OpenChannel) channel, message, messageGroupType);
        }
    }

    @Override
    @NonNull
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
