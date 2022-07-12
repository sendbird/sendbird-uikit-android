package com.sendbird.uikit.customsample.groupchannel.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.params.MessageListParams;
import com.sendbird.uikit.customsample.models.CustomMessageType;
import com.sendbird.uikit.vm.ChannelViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the customized <code>ChannelViewModel</code> to manage data related to the <code>GroupChannel</code>.
 */
public class CustomChannelViewModel extends ChannelViewModel {
    public CustomChannelViewModel(@NonNull String channelUrl, @Nullable MessageListParams messageListParams) {
        super(channelUrl, messageListParams);
    }

    @NonNull
    @Override
    public MessageListParams createMessageListParams() {
        final MessageListParams params = super.createMessageListParams();
        final List<String> customTypes = new ArrayList<>();
        customTypes.add(CustomMessageType.NONE.getValue());
        customTypes.add(CustomMessageType.HIGHLIGHT.getValue());
        customTypes.add(CustomMessageType.EMOJI.getValue());
        params.setCustomTypes(customTypes);
        return params;
    }
}
