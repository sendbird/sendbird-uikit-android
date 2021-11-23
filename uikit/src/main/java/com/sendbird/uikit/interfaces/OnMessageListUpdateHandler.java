package com.sendbird.uikit.interfaces;

import androidx.annotation.NonNull;

import com.sendbird.android.BaseMessage;

import java.util.List;

public interface OnMessageListUpdateHandler {
    void onListUpdated(@NonNull List<BaseMessage> messages);
}
