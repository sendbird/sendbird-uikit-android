package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import com.sendbird.android.channel.BaseChannel;
import com.sendbird.android.message.BaseMessage;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.databinding.SbViewAdminMessageBinding;
import com.sendbird.uikit.internal.ui.messages.AdminMessageView;

import java.util.Map;

public final class AdminMessageViewHolder extends MessageViewHolder {
    @NonNull
    private final AdminMessageView adminMessageView;

    AdminMessageViewHolder(@NonNull SbViewAdminMessageBinding binding, boolean useMessageGroupUI) {
        super(binding.getRoot(), useMessageGroupUI);
        adminMessageView = binding.adminMessageView;
    }

    @Override
    public void bind(@NonNull BaseChannel channel, @NonNull BaseMessage message, @NonNull MessageGroupType messageGroupType) {
        adminMessageView.setMessageUIConfig(messageUIConfig);
        adminMessageView.drawMessage(message);
    }

    @Override
    @NonNull
    public Map<String, View> getClickableViewMap() {
        return clickableViewMap;
    }
}
