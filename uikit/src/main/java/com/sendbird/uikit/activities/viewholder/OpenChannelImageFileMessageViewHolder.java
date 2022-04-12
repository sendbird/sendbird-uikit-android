package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.OpenChannel;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileImageMessageBinding;
import com.sendbird.uikit.widgets.OpenChannelImageFileMessageView;

import java.util.Map;

public final class OpenChannelImageFileMessageViewHolder extends MessageViewHolder {
    @NonNull
    private final OpenChannelImageFileMessageView openChannelImageFileMessageView;

    OpenChannelImageFileMessageViewHolder(@NonNull SbViewOpenChannelFileImageMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        openChannelImageFileMessageView = binding.openChannelImageFileMessageView;
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), openChannelImageFileMessageView.getBinding().ivThumbnailOveray);
        clickableViewMap.put(ClickableViewIdentifier.Profile.name(), openChannelImageFileMessageView.getBinding().ivProfileView);
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        if (channel instanceof OpenChannel) {
            openChannelImageFileMessageView.drawMessage((OpenChannel) channel, message, messageGroupType);
        }
    }

    @Override
    @NonNull
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
