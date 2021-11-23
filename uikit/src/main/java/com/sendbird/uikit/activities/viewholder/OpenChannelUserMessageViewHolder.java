package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.BR;
import com.sendbird.uikit.consts.ClickableViewIdentifier;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.widgets.OpenChannelUserMessageView;

import java.util.Map;

public final class OpenChannelUserMessageViewHolder extends MessageViewHolder {
    OpenChannelUserMessageViewHolder(@NonNull ViewDataBinding binding, boolean useMessageGroupUI) {
        super(binding, useMessageGroupUI);
        final OpenChannelUserMessageView root = ((OpenChannelUserMessageView) binding.getRoot());
        clickableViewMap.put(ClickableViewIdentifier.Chat.name(), root.getBinding().contentPanel);
        clickableViewMap.put(ClickableViewIdentifier.Profile.name(), root.getBinding().ivProfileView);
    }

    @Override
    public void bind(BaseChannel channel, @NonNull BaseMessage message, MessageGroupType messageGroupType) {
        binding.setVariable(BR.channel, channel);
        binding.setVariable(BR.message, message);
        binding.setVariable(BR.messageGroupType, messageGroupType);
    }

    @Override
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
