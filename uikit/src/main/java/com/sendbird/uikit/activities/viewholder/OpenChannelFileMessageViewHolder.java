package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.channel.OpenChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelFileMessageBinding;
import com.sendbird.uikit.internal.ui.messages.OpenChannelFileMessageView;

import java.util.Map;

public final class OpenChannelFileMessageViewHolder extends MessageViewHolder {
    @NonNull
    private final OpenChannelFileMessageView openChannelFileMessageView;

    OpenChannelFileMessageViewHolder(@NonNull SbViewOpenChannelFileMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        openChannelFileMessageView = binding.openChannelFileMessageView;
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), openChannelFileMessageView.getBinding().contentPanel);
        clickableViewMap.put(ClickableViewIdentifier.Profile.name(), openChannelFileMessageView.getBinding().ivProfileView);
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        openChannelFileMessageView.setMessageUIConfig(messageUIConfig);
        if (channel instanceof OpenChannel) {
            openChannelFileMessageView.drawMessage((OpenChannel) channel, message, messageGroupType);
        }
    }

    @NonNull
    @Override
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
