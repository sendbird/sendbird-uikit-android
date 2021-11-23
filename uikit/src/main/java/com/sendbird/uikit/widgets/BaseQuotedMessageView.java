package com.sendbird.uikit.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sendbird.android.BaseMessage;

public abstract class BaseQuotedMessageView extends FrameLayout {

    public BaseQuotedMessageView(@NonNull Context context) {
        super(context);
    }

    public BaseQuotedMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseQuotedMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void drawQuotedMessage(@Nullable BaseMessage message);
}
