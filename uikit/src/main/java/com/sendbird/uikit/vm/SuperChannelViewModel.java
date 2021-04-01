package com.sendbird.uikit.vm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.GroupChannel;
import com.sendbird.android.MessageListParams;

public class SuperChannelViewModel extends ChannelViewModel {
    SuperChannelViewModel(@NonNull GroupChannel groupChannel, @Nullable MessageListParams params) {
        super(groupChannel, params);
    }
}
