package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileVideoMessageBinding;
import com.sendbird.uikit.widgets.OpenChannelVideoFileMessageView;

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
