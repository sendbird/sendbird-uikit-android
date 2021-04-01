package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.BR;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.widgets.OpenChannelUserMessageView;

public class OpenChannelUserMessageViewHolder extends OpenChannelMessageViewHolder {
    private final View clickableView;

    OpenChannelUserMessageViewHolder(@NonNull ViewDataBinding binding, boolean useMessageGroupUI) {
        super(binding, useMessageGroupUI);
        this.clickableView = ((OpenChannelUserMessageView) binding.getRoot()).getBinding().contentPanel;
    }

    @Override
    public View getProfileView() {
        return ((OpenChannelUserMessageView) binding.getRoot()).getBinding().ivProfileView;
    }

    @Override
    public void bind(BaseChannel channel, @NonNull BaseMessage message, MessageGroupType messageGroupType) {
        binding.setVariable(BR.channel, channel);
        binding.setVariable(BR.message, message);
        binding.setVariable(BR.messageGroupType, messageGroupType);
    }

    @Override
    public View getClickableView() {
        return clickableView;
    }
}
