package com.sendbird.uikit.activities.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;

import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.uikit.BR;
import com.sendbird.uikit.consts.MessageGroupType;
import com.sendbird.uikit.widgets.OpenChannelFileMessageView;

public class OpenChannelFileMessageViewHolder extends OpenChannelMessageViewHolder {
    private final View clickableView;

    OpenChannelFileMessageViewHolder(@NonNull ViewDataBinding binding, boolean useMessageGroupUI) {
        super(binding, useMessageGroupUI);
        clickableView = ((OpenChannelFileMessageView) binding.getRoot()).getBinding().contentPanel;
    }

    @Override
    public View getProfileView() {
        return ((OpenChannelFileMessageView) binding.getRoot()).getBinding().ivProfileView;
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
