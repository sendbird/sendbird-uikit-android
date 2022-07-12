package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewOpenChannelAdminMessageBinding;
import com.sendbird.uikit.widgets.OpenChannelAdminMessageView;

import java.util.Map;

public final class OpenChannelAdminMessageViewHolder extends MessageViewHolder {
    @NonNull
    private final OpenChannelAdminMessageView openChannelAdminMessageView;

    OpenChannelAdminMessageViewHolder(@NonNull SbViewOpenChannelAdminMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        openChannelAdminMessageView = binding.openChannelAdminMessageView;
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        openChannelAdminMessageView.drawMessage(message);
    }

    @NonNull
    @Override
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
